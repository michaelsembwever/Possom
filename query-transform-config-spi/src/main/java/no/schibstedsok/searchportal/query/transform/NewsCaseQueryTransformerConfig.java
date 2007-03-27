// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;


import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

/**
 * Checks if the query should be transformed from a ejb lookup on the queryString. Transformation will replace
 * the whole query.
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. It uses the raw querystring
 * to transform the query. All transforms to the resulting query should be done after this.
 */
@Controller("NewsCaseQueryTransformer")
public final class NewsCaseQueryTransformerConfig extends AbstractQueryTransformerConfig {

    private static final String QUERY_TYPE = "query-type";
    private String queryType;

    /**
     *
     * @return
     */
    public String getQueryType(){
        return queryType;
    }

    @Override
    public NewsCaseQueryTransformerConfig readQueryTransformer(final Element element) {
        queryType = element.getAttribute(QUERY_TYPE);
        return this;
    }

}
