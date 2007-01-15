// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
public final class CatalogueInfopageQueryTransformer extends AbstractQueryTransformer {
    
    public String getTransformedQuery() {

        final String originalQuery = getContext().getTransformedQuery();

        return "iypcompanyid:" + originalQuery;
    }
}
