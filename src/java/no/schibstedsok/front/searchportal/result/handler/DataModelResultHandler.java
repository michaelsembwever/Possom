/*
 * DataModelResultHandler.java
 *
 * Created on May 26, 2006, 4:11 PM
 *
 */

package no.schibstedsok.front.searchportal.result.handler;

import java.util.Hashtable;
import java.util.Map;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.util.PagingDisplayHelper;
import no.schibstedsok.front.searchportal.view.config.SearchTab;
import org.apache.log4j.Logger;

/**
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
    
    /** Creates a new instance of DataModelResultHandler */
    public DataModelResultHandler() {
    }
    
    // Public --------------------------------------------------------
    
    // ResultHandler implementation ----------------------------------------------
    
    public void handleResult(final Context cxt, final Map parameters) {
        
        final SearchTab tab = cxt.getSearchTab();
        final SearchConfiguration config = cxt.getSearchResult().getSearchCommand().getSearchConfiguration();
        
        // simple beginnngs of the datamodel handler
        //  currently only puts into the request what we'll need at the decorator level.
        //   copy these over from VelocityResultHandler.populateContext() as needed.
        
        // results
        if( parameters.get("results") == null ){
            parameters.put("results", new Hashtable<String,SearchResult>());
            LOG.debug(DEBUG_CREATED_RESULTS);
        }
        final Hashtable<String,SearchResult> results 
                = (Hashtable<String,SearchResult>)parameters.get("results");

        LOG.debug(DEBUG_ADD_RESULT + config.getName());
        results.put(config.getName(), cxt.getSearchResult());
        
        // Paging helper
        if (config.isPagingEnabled()) {
            final PagingDisplayHelper pager = new PagingDisplayHelper(cxt.getSearchResult().getHitCount(), 
                    config.getResultsToReturn(), tab.getPageSize());

            final Object v = parameters.get("offset");
            pager.setCurrentOffset(Integer.parseInt( v instanceof String[] && ((String[])v).length ==1
                    ? ((String[]) v)[0]
                    : (String) v));
            
            if( parameters.get("pagers") == null ){
                parameters.put("pagers", new Hashtable<String,PagingDisplayHelper>());
            }
            final Hashtable<String,PagingDisplayHelper> pagers 
                    = (Hashtable<String,PagingDisplayHelper>)parameters.get("pagers");
            
            pagers.put(config.getName(), pager);
        }
    }
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------

    
}
