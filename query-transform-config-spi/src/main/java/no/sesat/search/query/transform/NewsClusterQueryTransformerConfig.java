package no.sesat.search.query.transform;

import no.sesat.search.query.transform.AbstractQueryTransformerConfig.Controller;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;


/**
 * Adds a filter to the query
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. It uses the raw querystring
 * to transform the query. All transforms to the resulting query should be done after this.
 */
@Controller("NewsClusterQueryTransformer")
public final class NewsClusterQueryTransformerConfig extends AbstractQueryTransformerConfig {


    private static final String PARAM_FIELDS = "param-fields";
    private static final String CLUSTER_FIELD = "cluster-field";
    private static final String CLUSTER_ID_FIELD = "cluster-id-field";
    private static final String TIMESTAMP_FIELD = "timestamp-field";
    private static final String MAX_AGE_IN_DAYS = "max-age-in-days";
    private static final String CLUSTER_FILTER = "cluster-filter";

    private String[] paramFields;
    private String timestampField;
    private String clusterField;
    private String clusterIdField = "clusterId";
    private int maxAgeInDays = 0;
    private boolean clusterFilter = true;


    /**
     * @return
     */
    public String[] getParamFields() {
        return paramFields;
    }

    public String getTimestampField() {
        return timestampField;
    }

    public String getClusterField() {
        return clusterField;
    }

    public String getClusterIdField() {
        return clusterIdField;
    }

    public boolean isClusterFilter() {
        return clusterFilter;
    }

    public void setClusterFilter(boolean clusterFilter) {
        this.clusterFilter = clusterFilter;
    }

    /**
     * I max age is set to 0, No max age is defined.
     *
     * @return
     */
    public int getMaxAgeInDays() {
        return maxAgeInDays;
    }

    @Override
    public NewsClusterQueryTransformerConfig readQueryTransformer(final Element element) {
        paramFields = StringUtils.split(element.getAttribute(PARAM_FIELDS), ",");
        clusterField = element.getAttribute(CLUSTER_FIELD);
        timestampField = element.getAttribute(TIMESTAMP_FIELD);
        final String clusterIdField = element.getAttribute(CLUSTER_ID_FIELD);
        if (clusterIdField != null && clusterIdField.length() > 0) {
            this.clusterIdField = clusterIdField;
        }
        final String maxAge = element.getAttribute(MAX_AGE_IN_DAYS);
        if (maxAge != null && maxAge.length() > 0) {
            maxAgeInDays = Integer.parseInt(maxAge);
        }
        final String clusterFilterString = element.getAttribute(CLUSTER_FILTER);
        if (clusterFilterString != null && clusterFilterString.equalsIgnoreCase("false")) {
            clusterFilter = false;
        } else {
            clusterFilter = true;
        }

        return this;
    }


}
