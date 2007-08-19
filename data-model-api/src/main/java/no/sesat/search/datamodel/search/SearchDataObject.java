/*
 * SearchDataObject.java
 * 
 * Created on 15/05/2007, 11:38:04
 * 
 */

package no.sesat.search.datamodel.search;

import no.sesat.search.datamodel.access.AccessAllow;
import static no.sesat.search.datamodel.access.ControlLevel.*;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.query.QueryDataObject;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.result.PagingDisplayHelper;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

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
