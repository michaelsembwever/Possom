// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;
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

    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String value = item.getField(sourceField);
            item.addField(targetField, StringEscapeUtils.escapeJavaScript(value));
        }
    }
}
