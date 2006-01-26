package no.schibstedsok.front.searchportal.result;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MultiValuedFieldCollector implements ResultHandler {

    private Map fieldMap = new HashMap();

    public void handleResult(Context cxt, Map parameters) {
        
        for (Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            for (Iterator fields = fieldMap.keySet().iterator(); fields.hasNext();) {
                String field = (String) fields.next();

                String fieldValue = searchResultItem.getField(field);
                String format = "";

                if (fieldValue != null) {
                    String[] values = StringUtils.split(fieldValue, ' ');

                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];

                        if (value.length() == 8) {
                            format = value.substring(0,2) + " " + value.substring(2,4) + " " + value.substring(4,6) + " " + value.substring(6,8);
                        } else
                            format = value;

                        searchResultItem.addToMultivaluedField((String) fieldMap.get(field), format);
                    }

                }
            }
        }
    }
}
