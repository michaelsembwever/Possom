package no.schibstedsok.front.searchportal.result;

import java.util.Collection;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchResultItem {
    void addField(String field, String value);
    String getField(String field);
    Collection getFieldNames();
    public Collection getMultivaluedField(String field);
    public void addToMultivaluedField(String field, String value);
}
