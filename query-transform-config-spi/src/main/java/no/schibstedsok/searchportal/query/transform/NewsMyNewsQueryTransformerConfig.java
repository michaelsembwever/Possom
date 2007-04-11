package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import org.w3c.dom.Element;

@Controller("NewsMyNewsQueryTransformer")
public class NewsMyNewsQueryTransformerConfig extends AbstractQueryTransformerConfig {
    private String cookieParameter;
    private String transformExpsession;
    private static final String COOKIE_PARAMETER = "cookie-parameter";
    private static final String TRANSFORM_EXPRESSION = "transform-expression";

    public String getCookieParameter() {
        return cookieParameter;
    }

    public void setCookieParameter(String cookieParameter) {
        this.cookieParameter = cookieParameter;
    }

    public String getTransformExpsession() {
        return transformExpsession;
    }

    public void setTransformExpsession(String transformExpsession) {
        this.transformExpsession = transformExpsession;
    }

    @Override
    public NewsMyNewsQueryTransformerConfig readQueryTransformer(final Element element) {
        cookieParameter = element.getAttribute(COOKIE_PARAMETER);
        transformExpsession = element.getAttribute(TRANSFORM_EXPRESSION);
        return this;
    }

}
