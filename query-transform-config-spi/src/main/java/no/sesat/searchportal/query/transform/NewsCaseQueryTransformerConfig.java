/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal.query.transform;


import no.sesat.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;
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
    private static final String UNCLUSTERED_DELAY = "unclustered-delay";
    private static final String UNCLUSTERED_DELAY_IN_MINUTES = "unclustered-delay-in-minutes";
    private static final String TIME_ZONE = "time-zone";

    private static final String DEFAULT_CONVERT_ELEMENT = "default-convert";
    private String timeZone = "UTC";
    private String queryType;
    private String queryParameter;
    private String typeParameter;
    private String defaultType;
    private boolean unclusteredDelayFilter = false;
    private int unclusteredDelayInMinutes = 10;
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


    public boolean isUnclusteredDelayFilter() {
        return unclusteredDelayFilter;
    }

    public int getUnclusteredDelayInMinutes() {
        return unclusteredDelayInMinutes;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public NewsCaseQueryTransformerConfig readQueryTransformer(final Element element) {
        queryType = element.getAttribute(QUERY_TYPE);
        if (element.getAttribute(QUERY_PARAMETER) != null && element.getAttribute(QUERY_PARAMETER).length() > 0) {
            queryParameter = element.getAttribute(QUERY_PARAMETER);
        }
        typeParameter = element.getAttribute(TYPE_PARAMETER);
        String optionalParameter = element.getAttribute(DEFAULT_TYPE);
        if (optionalParameter != null && optionalParameter.length() > 0) {
            defaultType = optionalParameter;
        }
        optionalParameter = element.getAttribute(UNCLUSTERED_DELAY);
        if (optionalParameter != null && optionalParameter.equalsIgnoreCase("true")) {
            unclusteredDelayFilter = true;
        }
        optionalParameter = element.getAttribute(UNCLUSTERED_DELAY_IN_MINUTES);
        if (optionalParameter != null && optionalParameter.length() > 0) {
            unclusteredDelayInMinutes = Integer.parseInt(optionalParameter);
        }
        optionalParameter = element.getAttribute(TIME_ZONE);
        if (optionalParameter != null && optionalParameter.length() > 0) {
            timeZone = optionalParameter;
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
                typeConversions.put(name, new String[]{prefix, postfix});
            }
        }
        return this;
    }

}
