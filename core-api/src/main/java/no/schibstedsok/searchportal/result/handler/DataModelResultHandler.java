// Copyright (2007) Schibsted Søk AS
/*
 * DataModelResultHandler.java
 *
 * Created on May 26, 2006, 4:11 PM
 *
 */

package no.schibstedsok.searchportal.result.handler;

import java.util.Map;
import java.util.Properties;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.DataModelFactory;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.query.QueryDataObject;
import no.schibstedsok.searchportal.datamodel.search.SearchDataObject;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.result.PagingDisplayHelper;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
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

        final SearchTab tab = cxt.getSearchTab();
        final SearchConfiguration config = cxt.getSearchConfiguration();
        final Map<String,Object> parameters = datamodel.getJunkYard().getValues();

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

        // Paging helper
        PagingDisplayHelper pager = null;
        if (config.isPaging()) {
            
            pager = new PagingDisplayHelper(
                    cxt.getSearchResult().getHitCount(), 
                    tab.getPageSize(), 
                    tab.getPagingSize());

            final Object v = null != parameters.get("offset") ? parameters.get("offset") : "0";
            pager.setCurrentOffset(Integer.parseInt( v instanceof String[] && ((String[])v).length ==1
                    ? ((String[]) v)[0]
                    : (String) v));

        }

        // Update the datamodel
        final QueryDataObject queryDO = factory.instantiate(
                QueryDataObject.class,
                new DataObject.Property("string", cxt.getQuery().getQueryString()),
                new DataObject.Property("query", cxt.getQuery()));
        
        final SearchDataObject searchDO = factory.instantiate(
                SearchDataObject.class,
                new DataObject.Property("configuration", cxt.getSearchConfiguration()),
                new DataObject.Property("query", queryDO),
                new DataObject.Property("results", cxt.getSearchResult()),
                new DataObject.Property("pager", pager));

        datamodel.setSearch(config.getName(), searchDO);
    }

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------


}
