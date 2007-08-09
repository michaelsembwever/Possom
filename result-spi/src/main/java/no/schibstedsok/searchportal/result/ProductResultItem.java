/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
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
