// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;


/**
 * Add iypcompanyid to the front of the original query, which is a companyid.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version $Revision:$
 */
public final class CatalogueInfopageQueryTransformer extends AbstractQueryTransformer {

    /**
     *
     * @param config
     */
    public CatalogueInfopageQueryTransformer(final QueryTransformerConfig config){}

    public String getTransformedQuery() {
        final String originalQuery = getContext().getTransformedQuery();
        return "iypcompanyid:" + originalQuery;
    }
}
