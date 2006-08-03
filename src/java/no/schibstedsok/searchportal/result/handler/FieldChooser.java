// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import no.schibstedsok.searchportal.result.SearchResultItem;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FieldChooser implements ResultHandler {

    private List fields = new ArrayList();
    private String targetField;

    public void handleResult(final Context cxt, final Map parameters) {
        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            for (final Iterator iterator1 = fields.iterator(); iterator1.hasNext();) {
                final String fieldName = (String) iterator1.next();
                if (item.getField(fieldName) != null) {
                    item.addField(targetField, item.getField(fieldName));
                    break;
                }
            }
        }
    }

    public void addField(final String fieldName) {
        fields.add(fieldName);
    }

    public void setTargetField(final String fieldName) {
        targetField = fieldName;
    }
}
