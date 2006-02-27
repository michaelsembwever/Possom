// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import no.schibstedsok.front.searchportal.result.SearchResultItem;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: ssthkjer
 * Date: 20.des.2005
 * Time: 14:37:50
 * To change this template use File | Settings | File Templates.
 */
public class ImageHelper implements ResultHandler {

    private Map fieldMap = new HashMap();

    public void handleResult(final Context cxt, final Map parameters) {

        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            for (final Iterator fields = fieldMap.keySet().iterator(); fields.hasNext();) {
                final String field = (String) fields.next();
                final String fieldValue = searchResultItem.getField(field);

                if (field.equals("ypmastermarks")) {

                    if (fieldValue != null) {
                        final String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            final String value = values[i];
                            searchResultItem.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                } else {
                    if (fieldValue != null) {
                        final String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            final String value = values[i];
                            searchResultItem.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                }
            }
        }
    }
}
