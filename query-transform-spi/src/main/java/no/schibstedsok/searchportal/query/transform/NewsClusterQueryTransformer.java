package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.Clause;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import java.util.Calendar;
import java.util.Map;
import java.text.SimpleDateFormat;

public class NewsClusterQueryTransformer extends AbstractQueryTransformer {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final static Logger LOG = Logger.getLogger(NewsClusterQueryTransformer.class);
    private static final String PARAM_FIELDS ="param-fields";
    private static final String CLUSTER_FIELD="cluster-field";
    private static final String CLUSTER_ID_FIELD="cluster-id-field";
    private static final String TIMESTAMP_FIELD="timestamp-field";
    private static final String MAX_AGE_IN_DAYS="max-age-in-days";

    private String[] paramFields;
    private String timestampField;
    private String clusterField;
    private String clusterIdField = "clusterId";
    private int maxAgeInDays = 7;
    
    public void visitImpl(final Clause clause) {
        // No transformation, only filter
    }

    @Override
    public String getFilter(final Map parameters) {
        StringBuilder filter = new StringBuilder();
        for (String paramField : paramFields) {            
            Object paramValue = parameters.get(paramField);
            LOG.debug("Adding param from datamodel: " + paramField + "=" + paramValue);
            if (paramValue != null && paramValue instanceof String) {
                filter.append(paramField).append(':');
                filter.append('\"').append((String) paramValue).append('\"');
                filter.append(" and ");
            }
        }
        Object clusterId = parameters.get(clusterIdField);
        if (clusterId != null && clusterId instanceof String) {
            filter.append("cluster:").append('\"').append((String) clusterId).append("\" ");
        } else {
            filter.append("cluster:range(1,max) ");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK, -maxAgeInDays);
        filter.append("and ").append(timestampField).append(':');
        filter.append("range(").append(sdf.format(calendar.getTime())).append(",max)");
        return filter.toString();
    }

    @Override
    public QueryTransformer readQueryTransformer(final Element element) {
        paramFields = StringUtils.split(element.getAttribute(PARAM_FIELDS),",");
        clusterField = element.getAttribute(CLUSTER_FIELD);
        timestampField = element.getAttribute(TIMESTAMP_FIELD);
        clusterIdField = element.getAttribute(CLUSTER_ID_FIELD);
        final String maxAge = element.getAttribute(MAX_AGE_IN_DAYS);
        if (maxAge != null) {
            maxAgeInDays = Integer.parseInt(maxAge);
        }
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final NewsClusterQueryTransformer ncqt = (NewsClusterQueryTransformer) super.clone();
        ncqt.paramFields = paramFields;
        ncqt.clusterField = clusterField;
        ncqt.maxAgeInDays = maxAgeInDays;
        ncqt.timestampField = timestampField;
        ncqt.clusterIdField = clusterIdField;
        return ncqt;
    }

}
