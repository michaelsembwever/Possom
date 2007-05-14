// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * Used to create a document summary. Basically concatinates two fields if both are peresent and not empty(For all
 * results) If not bothe fields are there it will populate the target field with a fallback field.
 *
 * @author Geir H. Pettersen (T-Rank)
 * @version $Id$
 */
public class DocumentSummaryResultHandler implements ResultHandler {
    
    private final DocumentSummaryResultHandlerConfig config;

    /**
     * @param config the config
     */
    public DocumentSummaryResultHandler(final ResultHandlerConfig config) {
        this.config = (DocumentSummaryResultHandlerConfig) config;

    }

    /**
     * {@inherit} *
     */
    public void handleResult(final Context cxt, final DataModel datamodel) {
        setDocumentSummary(cxt.getSearchResult());
    }

    private void setDocumentSummary(final ResultList<ResultItem> searchResult) {
        
        if (searchResult != null && searchResult.getResults().size() > 0) {
            for (ResultItem searchResultItem : searchResult.getResults()) {
                if (searchResultItem instanceof ResultList<?>) {
                    setDocumentSummary((ResultList<ResultItem>)searchResultItem);
                }
                
                final String firstSummaryFieldValue = searchResultItem.getField(config.getFirstSummaryField());
                final String secondSummaryFieldValue = searchResultItem.getField(config.getSecondSummaryField());
                final StringBuilder targetFieldValue = new StringBuilder();
                
                if (firstSummaryFieldValue != null 
                        && secondSummaryFieldValue != null 
                        && firstSummaryFieldValue.length() > 0 
                        && secondSummaryFieldValue.length() > 0) {
                    
                    targetFieldValue.append(firstSummaryFieldValue);
                    targetFieldValue.append(config.getFieldSeparator());
                    targetFieldValue.append(secondSummaryFieldValue);
                } else {
                    targetFieldValue.append(searchResultItem.getField(config.getFallbackField()));
                }
                
                searchResult.replaceResult(
                        searchResultItem, 
                        searchResultItem.addField(config.getTargetField(), targetFieldValue.toString())
                    );
            }
        }
    }
}
