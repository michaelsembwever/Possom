package no.sesat.search.query.transform;

import no.sesat.search.datamodel.junkyard.JunkYardDataObject;
import no.sesat.search.query.Clause;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Adds a filter to the query
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. It uses the raw querystring
 * to transform the query. All transforms to the resulting query should be done after this.
 */
public final class NewsClusterQueryTransformer extends AbstractQueryTransformer {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final static Logger LOG = Logger.getLogger(NewsClusterQueryTransformer.class);


    private final NewsClusterQueryTransformerConfig config;

    /**
     * @param config
     */
    public NewsClusterQueryTransformer(final QueryTransformerConfig config) {
        this.config = (NewsClusterQueryTransformerConfig) config;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * @param clause
     */
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
        // Using Junkyard, since the parameters seems to be incomplete...
        sb.append(getQueryFilter(getContext().getDataModel().getJunkYard()));
        if (!emptyQuery) {
            sb.append(',');
            sb.append(originalQuery);
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * @param parameters
     * @return
     */
    public String getQueryFilter(final JunkYardDataObject parameters) {
        StringBuilder filter = new StringBuilder();
        int paramAddCount = 0;
        for (String paramField : config.getParamFields()) {
            String paramValue = (String) parameters.getValue(paramField);
            LOG.debug("Adding param from datamodel: " + paramField + "=" + paramValue);
            if (paramValue != null) {
                if (paramAddCount > 0) {
                    filter.append(" and ");
                }
                filter.append(paramField).append(':');
                filter.append("equals(");
                filter.append('\"').append(paramValue).append("\")");
                paramAddCount++;
            }
        }
        String clusterId = (String) parameters.getValue(config.getClusterIdField());
        if (clusterId != null) {
            if (paramAddCount > 0) {
                filter.append(" and ");
            }
            filter.append(config.getClusterField()).append(":").append('\"').append(clusterId).append("\" ");
        } else if (config.isClusterFilter()) {
            if (paramAddCount > 0) {
                filter.append(" and ");
            }
            filter.append(config.getClusterField()).append(":range(1,max) ");
            if (config.getMaxAgeInDays() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                calendar.add(Calendar.DAY_OF_WEEK, -config.getMaxAgeInDays());
                filter.append("and ").append(config.getTimestampField()).append(':');
                filter.append("range(").append(sdf.format(calendar.getTime())).append(",max)");
            }

        }
        return filter.toString();
    }


}
