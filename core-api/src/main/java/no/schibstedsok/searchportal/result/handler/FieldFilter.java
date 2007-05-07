package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;


/**
 * @author Geir H. Pettersen (T-Rank)
 */
public class FieldFilter implements ResultHandler {
    private FieldFilterResultHandlerConfig config;

    public FieldFilter(final ResultHandlerConfig config) {
        this.config = (FieldFilterResultHandlerConfig) config;
    }

    public void handleResult(Context cxt, DataModel datamodel) {
        SearchResult searchResult = cxt.getSearchResult();
        filterResult(searchResult);
    }

    private void filterResult(SearchResult searchResult) {
        for (SearchResultItem searchResultItem : searchResult.getResults()) {
            if (config.getRecursiveField() != null) {
                SearchResult subResult = searchResultItem.getNestedSearchResult(config.getRecursiveField());
                if (subResult != null) {
                    filterResult(subResult);
                }
            }
            if (shouldFilter(searchResultItem)) {
                filter(searchResultItem);
            }
        }
    }

    private void filter(SearchResultItem searchResultItem) {
        for (String removeField : config.getRemoveFieldsArray()) {
            searchResultItem.addField(removeField, null);
        }
    }

    private boolean shouldFilter(SearchResultItem searchResultItem) {
        String filterSource = searchResultItem.getField(config.getFilterSrc());
        return filterSource != null && config.getMatchListSet().contains(filterSource.toLowerCase().trim());
    }

}
