package no.schibstedsok.searchportal.result;

import java.util.Collection;

public interface ProductResultItem {

	void addField(String field, String value);
    String getField(String field);
    public Collection getMultivaluedField(final String field);
    public void addToMultivaluedField(final String field, final String value);
}
