// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MultiValuedFieldCollector implements ResultHandler {

    private Map fieldMap = new HashMap();

    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            for (final Iterator fields = fieldMap.keySet().iterator(); fields.hasNext();) {
                final String field = (String) fields.next();

                final String fieldValue = item.getField(field);
                String format = "";

                if (fieldValue != null) {
                    final String[]  values = StringUtils.split(fieldValue, ' ');

                    for (int i = 0; i < values.length; i++) {
                        final String value = values[i];

                        if (value.length() == 8) {
                            format = value.substring(0, 2) + " " + value.substring(2, 4) + " " + value.substring(4, 6) + " " + value.substring(6, 8);
                        } else
                            format = value;

                        item.addToMultivaluedField((String) fieldMap.get(field), format);
                    }

                }
            }
        }
    }
}
