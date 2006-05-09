// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import no.schibstedsok.front.searchportal.result.SearchResultItem;

import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: ssthkjer
 * Date: 20.des.2005
 * Time: 14:37:50
 * To change this template use File | Settings | File Templates.
 */
public class ImageHelper implements ResultHandler {

    private Map<String,String> fieldMap = new HashMap<String,String>();

    public void addField(final String field, final String as){
        fieldMap.put(field, as);
    }

    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {

            for (final Iterator fields = fieldMap.keySet().iterator(); fields.hasNext();) {
                final String field = (String) fields.next();
                final String fieldValue = item.getField(field);

                if (field.equals("ypmastermarks")) {

                    if (fieldValue != null) {
                        final String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            final String value = values[i];
                            item.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                } else {
                    if (fieldValue != null) {
                        final String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            final String value = values[i];
                            item.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                }
            }
        }
    }
}
