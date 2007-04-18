// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;


import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

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
    private static final String QUERY_PARAMETER = "query-parameter";
    private static final String TYPE_PARAMETER = "type-parameter";
    private static final String PREFIX = "prefix";
    private static final String POSTFIX = "postfix";
    private static final String TYPE_NAME = "name";
    private static final String DEFAULT_TYPE = "default-type";

    private static final String DEFAULT_CONVERT_ELEMENT = "default-convert";
    private String queryType;
    private String queryParameter;
    private String typeParameter;
    private String defaultType;
    private Map<String, String[]> typeConversions;


    /**
     * @return
     */
    public String getQueryType() {
        return queryType;
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public String getTypeParameter() {
        return typeParameter;
    }

    public Map<String, String[]> getTypeConversions() {
        return typeConversions;
    }

    public String getDefaultType() {
        return defaultType;
    }

    @Override
    public NewsCaseQueryTransformerConfig readQueryTransformer(final Element element) {
        Logger log = Logger.getLogger(NewsCaseQueryTransformerConfig.class);
        queryType = element.getAttribute(QUERY_TYPE);
        if (element.getAttribute(QUERY_PARAMETER) != null && element.getAttribute(QUERY_PARAMETER).length() > 0) {
            queryParameter = element.getAttribute(QUERY_PARAMETER);
        }
        typeParameter = element.getAttribute(TYPE_PARAMETER);
        final String optionalParameter = element.getAttribute(DEFAULT_TYPE);
        if (optionalParameter != null && optionalParameter.length() > 0) {
            defaultType = optionalParameter;
        }
        NodeList convertNodeList = element.getElementsByTagName(DEFAULT_CONVERT_ELEMENT);
        if (convertNodeList.getLength() > 0) {
            typeConversions = new HashMap<String, String[]>();
            for (int i = 0; i < convertNodeList.getLength(); i++) {
                final Node n = convertNodeList.item(i);
                final Element convertElement = (Element) n;
                final String name = convertElement.getAttribute(TYPE_NAME);
                final String prefix = convertElement.getAttribute(PREFIX);
                final String postfix = convertElement.getAttribute(POSTFIX);

                log.debug("qwerty Adding conversion: " + name + "=" + prefix + "$" + postfix);
                typeConversions.put(name, new String[]{prefix, postfix});
            }
        }
        return this;
    }

}
