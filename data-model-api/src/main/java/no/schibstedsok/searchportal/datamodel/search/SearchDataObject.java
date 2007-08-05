/*
 * SearchDataObject.java
 * 
 * Created on 15/05/2007, 11:38:04
 * 
 */

package no.schibstedsok.searchportal.datamodel.search;

import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.query.QueryDataObject;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.result.PagingDisplayHelper;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/** Contains Search Command and Result information.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface SearchDataObject {
    
    SearchConfiguration getConfiguration();
    
    void setConfiguration(SearchConfiguration configuration);
        
    /**
     * 
     * @return 
     */
    ResultList<ResultItem> getResults();

    /**
     * 
     * @param results 
     */
    @AccessAllow({})
    void setResults(ResultList<ResultItem> results);

    // QueryDataObject ------------------------------------------------------------

    /**
     * 
     * @return 
     */
    QueryDataObject getQuery();

    /**
     * 
     * @param query 
     */
    @AccessAllow({})
    void setQuery(QueryDataObject query);
    
    
}
