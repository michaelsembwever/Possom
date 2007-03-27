// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;


/**
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public final class WeatherinfopageQueryTransformer extends AbstractQueryTransformer {

    /**
     *
     * @param config
     */
    public WeatherinfopageQueryTransformer(final QueryTransformerConfig config){
    }

    public String getTransformedQuery() {

        final String originalQuery = getContext().getTransformedQuery();

        return "igeneric1:" + originalQuery;
    }
}
