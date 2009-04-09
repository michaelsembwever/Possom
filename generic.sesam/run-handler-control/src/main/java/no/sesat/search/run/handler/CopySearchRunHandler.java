/*
 * Copyright (2008) Schibsted ASA
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
 */

package no.sesat.search.run.handler;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.commons.ref.ReferenceMap;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.apache.log4j.Logger;



/**
 * Copy a SearchDataObject (with a new name).
 *
 * The results is an instance of BasicResultList<BasicResultItem> with
 *  the list re-constructed with BasicResultList's copy constructor, and
 *   each item re-constructed with BasicResultItem's copy constructor.
 * Be warned as particular functionality belonging to an implementation of ResultList or ResultItem
 *  other than BasicResultList and BasicResultItem may be lost.
 *
 * @version $Id$
 *
 */
public final class CopySearchRunHandler implements RunHandler{

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 64;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.75f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 64;

    /** !Concurrent-safe! weak cache of DataModelFactories
     *  since hitting DataModelFactory.instanceOf(..) hard becomes a bottleneck.
     * read https://jira.sesam.no/jira/browse/SEARCH-3541 for more.
     */
    private static final ReferenceMap<Site,DataModelFactory> FACTORY_CACHE
            = new ReferenceMap<Site,DataModelFactory>(
                ReferenceMap.Type.WEAK,
                new ConcurrentHashMap<Site,Reference<DataModelFactory>>(
                    WEAK_CACHE_INITIAL_CAPACITY,
                    WEAK_CACHE_LOAD_FACTOR,
                    WEAK_CACHE_CONCURRENCY_LEVEL));

    private static final Logger LOG = Logger.getLogger(CopySearchRunHandler.class);

    private final CopySearchRunHandlerConfig config;


    public CopySearchRunHandler(final RunHandlerConfig rhc) {
        config =  (CopySearchRunHandlerConfig) rhc;
    }


    public void handleRunningQuery(final Context context) {


        final DataModelFactory factory = getDataModelFactory(context);
        final DataModel datamodel = context.getDataModel();

        final SearchDataObject from = datamodel.getSearch(config.getFrom());

        if(null != from){
            final ResultList<ResultItem> resultList = new BasicResultList<ResultItem>(from.getResults());
            for(ResultItem item : from.getResults().getResults()){
                resultList.addResult(new BasicResultItem(item));
            }

            final SearchDataObject searchDO = factory.instantiate(
                    SearchDataObject.class,
                    datamodel,
                    new DataObject.Property("configuration", from.getConfiguration()),
                    new DataObject.Property("query", from.getQuery()),
                    new DataObject.Property("results", resultList));

            datamodel.setSearch(config.getTo(), searchDO);

        }else{

            LOG.error("from attribute in <copy-search from=\"someCommandName\" to=\"someCommandName\" "
                    + "is not an existing search command");
        }

    }

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
            //  DataModelFactory.instanceOf(cxt) uses a ReentrantReadWriteLock in high-concurrency environment like here.
            // this is why we keep a local weak cache of the factories.
            //  the alternative would be to pollute DataModelFactory will this performance consideration and
            //  replace the ReentrantReadWriteLock that provides a synchronised api with a ConcurrentHashMap.
            return DataModelFactory.instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, cxt));

        }catch(SiteKeyedFactoryInstantiationException skfie){
            LOG.error(skfie.getMessage(), skfie);
            throw new IllegalStateException(skfie.getMessage(), skfie);
        }
    }

}
