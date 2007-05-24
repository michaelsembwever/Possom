// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.Collection;
import java.util.HashMap;

/** @deprecated use ResultItem instead **/
public interface ProductResultItem {

	void addField(String field, String value);

	String getField(String field);

	HashMap<String,Object> getFields();

	void setFields(HashMap<String,Object> map);

	Collection<String> getMultivaluedField(String field);

	void addToMultivaluedField(String field, String value);
}
