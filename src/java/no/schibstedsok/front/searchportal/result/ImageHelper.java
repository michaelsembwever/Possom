package no.schibstedsok.front.searchportal.result;

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

    public void handleResult(Context cxt, Map parameters) {
        
        final SearchResult result = cxt.getSearchResult();
        for (Iterator iterator = result.getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            for (Iterator fields = fieldMap.keySet().iterator(); fields.hasNext();) {
                String field = (String) fields.next();
                String fieldValue = searchResultItem.getField(field);

                if (field.equals("ypmastermarks")) {

                    if (fieldValue != null) {
                        String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            String value = values[i];
                            searchResultItem.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                } else {
                    if (fieldValue != null) {
                        String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            String value = values[i];
                            searchResultItem.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                }
            }
        }
    }
}
