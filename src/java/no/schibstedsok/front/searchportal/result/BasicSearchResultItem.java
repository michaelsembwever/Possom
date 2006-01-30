package no.schibstedsok.front.searchportal.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * A simple implementation of a search result item.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class BasicSearchResultItem implements SearchResultItem {

    private HashMap fields = new HashMap();

    public void addField(String field, String value) {
        fields.put(field, value);
    }

    public String getField(String field) {
        String fieldValue = (String) fields.get(field);

        if (fieldValue != null && (fieldValue.equals("  ") || fieldValue.equals(" "))) {
            return null;
        } else {
            return fieldValue;
        }
    }

    public Object getFieldAsObject(String field) {
        return fields.get(field);
    }
    
    public Integer getInteger(String field) {
        String fieldValue = (String) fields.get(field);

        if (fieldValue != null) {
            return new Integer(Integer.parseInt(fieldValue));
        } else {
            return null;
        }
    }

    public String getField(String field, int maxLength) {
        String fieldValue = (String) fields.get(field);

        if (fieldValue != null) {
            if (fieldValue.equals("  ")) {
                return null;
            } else {
                if (fieldValue.length() > maxLength) {
                    return fieldValue.substring(0, maxLength - 2 - 1) + "...";
                } else {
                }
            }
        }
        return fieldValue;
    }

    public Collection getFieldNames() {
        return fields.keySet();
    }

    public Collection getMultivaluedField(String field) {
        return (Collection) fields.get(field);
    }

    public void addToMultivaluedField(String field, String value) {
        if (! fields.containsKey(field)) {
            fields.put(field, new ArrayList());
        }

        Collection previousValues = (Collection) fields.get(field);
        previousValues.add(value);
    }

    public boolean equals(Object obj) {
        SearchResultItem other = (SearchResultItem) obj;
        
        if (other.getField("recordid") != null && getField("recordid") != null) {
            return getField("recordid").equals(other.getField("recordid"));
        } else {
            return super.equals(obj);
        }
    }

    public int hashCode() {
        
        if (getField("recordid") != null) {
            return getField("recordid").hashCode();
        } else {
            return super.hashCode();
        }
    }
}
