// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.Collection;

/**
 * @deprecated ResultItem is the replacement. migration in progress.
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchResultItem extends ResultItem{
    void addField(String field, String value);
    void addNestedSearchResult(String field, SearchResult nestedResult);
    SearchResult getNestedSearchResult(String field);
    String getField(String field);
    Object getFieldAsObject(String field);
    void addObjectField(String field, Object value);
    Collection getFieldNames();
    public Collection getMultivaluedField(String field);
    public void addToMultivaluedField(String field, String value);
}
