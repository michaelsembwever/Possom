/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * DataModelResultHandler.java
 *
 * Created on May 26, 2006, 4:11 PM
 *
 */

package no.sesat.search.result.handler;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.commons.ref.ReferenceMap;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.query.QueryDataObject;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;

/** Handles the insertion of the results into the datamodel.
 * This class must remain safe under multi-threaded conditions.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class DataModelResultHandler implements ResultHandler{


    // Constants -----------------------------------------------------

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 64;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.75f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 64;

    /** !Concurrent-safe! weak cache of DataModelFactories
     *  since hitting DataModelFactory.valueOf(..) hard becomes a bottleneck.
     * read https://jira.sesam.no/jira/browse/SEARCH-3541 for more.
     */
    private static final ReferenceMap<Site,DataModelFactory> FACTORY_CACHE
            = new ReferenceMap<Site,DataModelFactory>(
                ReferenceMap.Type.WEAK,
                new ConcurrentHashMap<Site,Reference<DataModelFactory>>(
                    WEAK_CACHE_INITIAL_CAPACITY,
                    WEAK_CACHE_LOAD_FACTOR,
                    WEAK_CACHE_CONCURRENCY_LEVEL));

    private static final Logger LOG = Logger.getLogger(DataModelResultHandler.class);
    private static final String DEBUG_ADD_RESULT = "Adding the result ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of DataModelResultHandler. Used directly by AbstractSearchCommand.
     */
    public DataModelResultHandler() {}

    // Public --------------------------------------------------------

    // ResultHandler implementation ----------------------------------------------

    public void handleResult(final Context cxt, final DataModel datamodel) {
        final SearchConfiguration config = cxt.getSearchConfiguration();

        // results
        LOG.debug(DEBUG_ADD_RESULT + config.getName());

        final DataModelFactory factory = getDataModelFactory(cxt);

        // friendly command-specific search string
        final String friendly = null != cxt.getDisplayQuery() && cxt.getDisplayQuery().length() > 0
                        ? cxt.getDisplayQuery()
                        : cxt.getQuery().getQueryString();

        // Update the datamodel
        final QueryDataObject queryDO = factory.instantiate(
                QueryDataObject.class,
                new DataObject.Property("string", friendly),
                new DataObject.Property("query", cxt.getQuery()));

        final SearchDataObject searchDO = factory.instantiate(
                SearchDataObject.class,
                new DataObject.Property("configuration", cxt.getSearchConfiguration()),
                new DataObject.Property("query", queryDO),
                new DataObject.Property("results", cxt.getSearchResult()));

        datamodel.setSearch(config.getName(), searchDO);

        // also ping everybody that might be waiting on these results: "dinner's served!"
        synchronized (datamodel.getSearches()) {
            datamodel.getSearches().notifyAll();
        }
    }

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private DataModelFactory getDataModelFactory(final Context cxt){

        final Site site = cxt.getSite();

        DataModelFactory factory = FACTORY_CACHE.get(site);

        if(null == factory){
            factory = getDataModelFactoryImpl(cxt);
            FACTORY_CACHE.put(site, factory);

        }

        return factory;
    }

    private DataModelFactory getDataModelFactoryImpl(final Context cxt){

        try{
            // application bottleneck https://jira.sesam.no/jira/browse/SEARCH-3541
            //  DataModelFactory.valueOf(cxt) uses a ReentrantReadWriteLock in high-concurrency environment like here.
            // this is why we keep a local weak cache of the factories.
            //  the alternative would be to pollute DataModelFactory will this performance consideration and
            //  replace the ReentrantReadWriteLock that provides a synchronised api with a ConcurrentHashMap.
            return DataModelFactory.valueOf(ContextWrapper.wrap(DataModelFactory.Context.class, cxt));

        }catch(SiteKeyedFactoryInstantiationException skfie){
            LOG.error(skfie.getMessage(), skfie);
            throw new IllegalStateException(skfie.getMessage(), skfie);
        }
    }

    // Inner classes -------------------------------------------------

}
