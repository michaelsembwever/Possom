package no.schibstedsok.front.searchportal.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FieldChooser implements ResultHandler {

    private List fields = new ArrayList();
    private String targetField;

    public void handleResult(SearchResult result, Map parameters) {
        for (Iterator iterator = result.getResults().iterator(); iterator.hasNext();) {
            SearchResultItem item = (SearchResultItem) iterator.next();

            for (Iterator iterator1 = fields.iterator(); iterator1.hasNext();) {
                String fieldName = (String) iterator1.next();
                if (item.getField(fieldName) != null) {
                    item.addField(targetField, item.getField(fieldName));
                    break;
                }
            }
        }
    }

    public void addField(String fieldName) {
        fields.add(fieldName);
    }

    public void setTargetField(String fieldName) {
        targetField = fieldName;
    }
}
