package no.schibstedsok.searchportal.result;

import java.util.Collection;
import java.util.HashMap;

public interface ProductResultItem {

	void addField(String field, String value);

	String getField(String field);

	HashMap getFields();

	void setFields(HashMap map);

	public Collection getMultivaluedField(final String field);

	public void addToMultivaluedField(final String field, final String value);
}
