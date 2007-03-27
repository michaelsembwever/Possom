// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id: 3359 $</tt>
 */
public final class InfopageQueryTransformer extends AbstractQueryTransformer {


    /**
     *
     * @param config
     */
    public InfopageQueryTransformer(final QueryTransformerConfig config){}


    public String getTransformedQuery() {

        final String originalQuery = getContext().getTransformedQuery();

        return "recordid:" + originalQuery;
    }
}
