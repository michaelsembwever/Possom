// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import no.schibstedsok.searchportal.view.StringChopper;

/**
 * A simple implementation of a search result item.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class BasicSearchResultItem implements SearchResultItem {

    private HashMap fields = new HashMap();

    public void addField(final String field, final String value) {
        fields.put(field, value);
    }

    public String getField(final String field) {
        final String fieldValue = (String) fields.get(field);

        if (fieldValue != null && (fieldValue.equals("  ") || fieldValue.equals(" "))) {
            return null;
        } else {
            return fieldValue;
        }
    }

    public Object getFieldAsObject(final String field) {
        return fields.get(field);
    }

    public void addObjectField(final String field, final Object value) {
        fields.put(field, value);
    }
    
    public Integer getInteger(final String field) {
        final String fieldValue = (String) fields.get(field);

        if (fieldValue != null) {
            return Integer.valueOf(Integer.parseInt(fieldValue));
        } else {
            return null;
        }
    }

    public String getField(final String field, final int maxLength) {
        final String fieldValue = (String) fields.get(field);

        if (fieldValue != null) {
            if (fieldValue.equals("  ")) {
                return null;
            } else {
                return StringChopper.chop(fieldValue, maxLength);
            }
        }
        return fieldValue;
    }

    public Collection getFieldNames() {
        return fields.keySet();
    }

    public Collection getMultivaluedField(final String field) {
        return (Collection) fields.get(field);
    }

    public void addToMultivaluedField(final String field, final String value) {
        if (! fields.containsKey(field)) {
            fields.put(field, new ArrayList());
        }

        final Collection previousValues = (Collection) fields.get(field);
        previousValues.add(value);
    }

    public boolean equals(final Object obj) {
        final SearchResultItem other = (SearchResultItem) obj;

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

    public void addNestedSearchResult(final String field, final SearchResult nestedResult) {
       fields.put(field, nestedResult);

    }

    public SearchResult getNestedSearchResult(final String field) {
       return (SearchResult) fields.get(field);
    }
}
