// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * Used to create a document summary. Basically concatinates two fields if both are peresent and not empty(For all
 * results) If not bothe fields are there it will populate the target field with a fallback field.
 *
 * @author Geir H. Pettersen (T-Rank)
 */
public class DocumentSummaryResultHandler implements ResultHandler {
    private DocumentSummaryResultHandlerConfig config;

    /**
     * @param config the config
     */
    public DocumentSummaryResultHandler(final ResultHandlerConfig config) {
        this.config = (DocumentSummaryResultHandlerConfig) config;

    }

    /**
     * {@inherit} *
     */
    public void handleResult(Context cxt, DataModel datamodel) {
        setDocumentSummary(cxt.getSearchResult());
    }

    private void setDocumentSummary(SearchResult searchResult) {
        if (searchResult != null && searchResult.getResults().size() > 0) {
            for (SearchResultItem searchResultItem : searchResult.getResults()) {
                if (config.getRecursiveField() != null) {
                    setDocumentSummary(searchResultItem.getNestedSearchResult(config.getRecursiveField()));
                }
                final String firstSummaryFieldValue = searchResultItem.getField(config.getFirstSummaryField());
                final String secondSummaryFieldValue = searchResultItem.getField(config.getSecondSummaryField());
                StringBuilder targetFieldValue = new StringBuilder();
                if (firstSummaryFieldValue != null && secondSummaryFieldValue != null && firstSummaryFieldValue.length() > 0 && secondSummaryFieldValue.length() > 0) {
                    targetFieldValue.append(firstSummaryFieldValue);
                    targetFieldValue.append(config.getFieldSeparator());
                    targetFieldValue.append(secondSummaryFieldValue);
                } else {
                    targetFieldValue.append(searchResultItem.getField(config.getFallbackField()));
                }
                searchResultItem.addField(config.getTargetField(), targetFieldValue.toString());
            }
        }
    }
}
