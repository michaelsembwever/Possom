package no.schibstedsok.searchportal.query.transform;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import no.schibstedsok.searchportal.query.transform.AbstractQueryTransformerConfig.Controller;


/**
 * Adds a filter to the query
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. It uses the raw querystring
 * to transform the query. All transforms to the resulting query should be done after this.
 */
@Controller("NewsClusterQueryTransformerConfig")
public final class NewsClusterQueryTransformerConfig extends AbstractQueryTransformerConfig {


    private static final String PARAM_FIELDS = "param-fields";
    private static final String CLUSTER_FIELD = "cluster-field";
    private static final String CLUSTER_ID_FIELD = "cluster-id-field";
    private static final String TIMESTAMP_FIELD = "timestamp-field";
    private static final String MAX_AGE_IN_DAYS = "max-age-in-days";

    private String[] paramFields;
    private String timestampField;
    private String clusterField;
    private String clusterIdField = "clusterId";
    private int maxAgeInDays = 7;

    /**
     *
     * @return
     */
    public String[] getParamFields(){
        return paramFields;
    }

    public String getTimestampField(){
        return timestampField;
    }

    public String getClusterField(){
        return clusterField;
    }

    public String getClusterIdField(){
        return clusterIdField;
    }

    public int getMaxAgeInDays(){
        return maxAgeInDays;
    }

    @Override
    public NewsClusterQueryTransformerConfig readQueryTransformer(final Element element) {
        paramFields = StringUtils.split(element.getAttribute(PARAM_FIELDS), ",");
        clusterField = element.getAttribute(CLUSTER_FIELD);
        timestampField = element.getAttribute(TIMESTAMP_FIELD);
        clusterIdField = element.getAttribute(CLUSTER_ID_FIELD);
        final String maxAge = element.getAttribute(MAX_AGE_IN_DAYS);
        if (maxAge != null) {
            maxAgeInDays = Integer.parseInt(maxAge);
        }
        return this;
    }


}
