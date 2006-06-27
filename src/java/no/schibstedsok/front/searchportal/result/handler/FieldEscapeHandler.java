// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * 
 */
public class FieldEscapeHandler implements ResultHandler {

    private String sourceField;
    private String targetField;
    
    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(final String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return targetField;
    }
    
    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }
    
    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String value = item.getField(sourceField);
            item.addField(targetField, StringEscapeUtils.escapeJavaScript(value));
        }
    }
}
