// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class InfopageQueryTransformer extends AbstractQueryTransformer {
    public String getTransformedQuery() {

        final String originalQuery = getContext().getTransformedQuery();

        return "recordid:" + originalQuery;
    }
}
