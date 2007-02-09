// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * A simple implementation of a product result item.
 *
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public class ProductSearchResultItem implements ProductResultItem {

    private HashMap fields = new HashMap();

    public void addField(final String field, final String value) {
        fields.put(field, value);
    }

    public String getField(final String field) {
        final String fieldValue = (String) fields.get(field);
        return fieldValue;
    }

    public Object getFieldAsObject(final String field) {
        return fields.get(field);
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

    //TODO:
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    //TODO:
    public int hashCode() {

        return super.hashCode();
    }

	public HashMap getFields() {
		return fields;
	}

	public void setFields(HashMap fields) {
		this.fields = fields;
	}

}
