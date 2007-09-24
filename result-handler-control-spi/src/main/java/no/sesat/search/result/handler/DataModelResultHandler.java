/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 **
 * DataModelResultHandler.java
 *
 * Created on May 26, 2006, 4:11 PM
 *
 */

package no.sesat.search.result.handler;

import java.util.Properties;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.query.QueryDataObject;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.config.PropertiesLoader;
import org.apache.log4j.Logger;

/** Handles the insertion of the results (& pager) into the datamodel.
 * This class must remain safe under multi-threaded conditions.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class DataModelResultHandler implements ResultHandler{


    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(DataModelResultHandler.class);
    private static final String DEBUG_CREATED_RESULTS = "Creating results Hashtable";
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

        final DataModelFactory factory;
        try{
            factory = DataModelFactory.valueOf(new DataModelFactory.Context(){
                public Site getSite() {
                    return datamodel.getSite().getSite();
                }
                public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                                                            final String resource,
                                                            final Properties properties) {
                    return cxt.newPropertiesLoader(siteCxt, resource, properties);
                }
            });
        }catch(SiteKeyedFactoryInstantiationException skfie){
            LOG.error(skfie.getMessage(), skfie);
            throw new IllegalStateException(skfie.getMessage(), skfie);
        }

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
    }

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------


}
