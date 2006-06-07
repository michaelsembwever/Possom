// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;


/**
 * @author <a href="mailto:larsj@conduct.no">Lars Johansson</a>
 * @version <tt>$Revision: 1 $</tt>
 */
public final class WeatherInfopageQueryTransformer extends AbstractQueryTransformer {
    
    public String getTransformedQuery() {

        final String originalQuery = getContext().getTransformedQuery();

        return "igeneric1:" + originalQuery;
    }
}
