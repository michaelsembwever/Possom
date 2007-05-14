package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;


/**
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public final class FieldFilter implements ResultHandler {
    
    private final FieldFilterResultHandlerConfig config;

    /**
     * 
     * @param config 
     */
    public FieldFilter(final ResultHandlerConfig config) {
        this.config = (FieldFilterResultHandlerConfig) config;
    }

    public void handleResult(final Context cxt, final DataModel datamodel) {
        ResultList<ResultItem> searchResult = cxt.getSearchResult();
        filterResult(searchResult);
    }

    private void filterResult(final ResultList<ResultItem> searchResult) {
        
        for (ResultItem searchResultItem : searchResult.getResults()) {
            if (searchResultItem instanceof ResultList<?>) {
                final ResultList<ResultItem> subResult = (ResultList<ResultItem>)searchResultItem;
                if (subResult != null) {
                    filterResult(subResult);
                }
            }
            
            if (shouldFilter(searchResultItem)) {
                
                searchResult.replaceResult(
                        searchResultItem, 
                        filter(searchResultItem)
                    );
            }
        }
    }

    private ResultItem filter(ResultItem searchResultItem) {
        
        for (String removeField : config.getRemoveFieldsArray()) {
            searchResultItem = searchResultItem.addField(removeField, null);
        }
        return searchResultItem;
    }

    private boolean shouldFilter(final ResultItem searchResultItem) {
        
        final String filterSource = searchResultItem.getField(config.getFilterSrc());
        return filterSource != null && config.getMatchListSet().contains(filterSource.toLowerCase().trim());
    }

}
