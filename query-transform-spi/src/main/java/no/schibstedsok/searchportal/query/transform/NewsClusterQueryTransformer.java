package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.query.Clause;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Adds a filter to the query
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. It uses the raw querystring
 * to transform the query. All transforms to the resulting query should be done after this.
 */
public class NewsClusterQueryTransformer extends AbstractQueryTransformer {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final static Logger LOG = Logger.getLogger(NewsClusterQueryTransformer.class);
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

    public void visitImpl(final Clause clause) {
        final String transformedQuery = transformQuery(getContext().getQuery().getQueryString());
        if (transformedQuery != null) {
            for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
                getContext().getTransformedTerms().put(keyClause, "");
            }
            LOG.debug("New query is: '" + transformedQuery + "'");
            if (transformedQuery.length() > 0) {
                getContext().getTransformedTerms().put(getContext().getQuery().getFirstLeafClause(), transformedQuery);
            }
        }
    }

    private String transformQuery(String originalQuery) {
        // No transformation, only filter
        StringBuilder sb = new StringBuilder();
        final boolean emptyQuery = originalQuery == null || originalQuery.trim().length() == 0;
        if (!emptyQuery) {
            sb.append("and(");
        }
        sb.append("filter(");
        sb.append(getQueryFilter(getContext().getDataModel().getParameters()));
        sb.append(')');
        if (!emptyQuery) {
            sb.append(',');
            sb.append(originalQuery);
            sb.append(')');
        }
        return sb.toString();
    }

    public String getQueryFilter(final ParametersDataObject parameters) {
        StringBuilder filter = new StringBuilder();
        for (String paramField : paramFields) {
            StringDataObject paramValue = parameters.getValue(paramField);
            LOG.debug("Adding param from datamodel: " + paramField + "=" + paramValue);
            if (paramValue != null) {
                filter.append(paramField).append(':');
                filter.append('\"').append(paramValue.getString()).append('\"');
                filter.append(" and ");
            }
        }
        StringDataObject clusterId = parameters.getValue(clusterIdField);
        if (clusterId != null) {
            filter.append(clusterField).append(":").append('\"').append(clusterId.getString()).append("\" ");
        } else {
            filter.append(clusterField).append(":range(1,max) ");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_WEEK, -maxAgeInDays);
        filter.append("and ").append(timestampField).append(':');
        filter.append("range(").append(sdf.format(calendar.getTime())).append(",max)");
        return filter.toString();
    }

    @Override
    public QueryTransformer readQueryTransformer(final Element element) {
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
