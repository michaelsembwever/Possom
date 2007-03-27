package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.request.ParametersDataObject;
import no.schibstedsok.searchportal.query.Clause;
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
     *
     * @param config
     */
    public NewsClusterQueryTransformer(final QueryTransformerConfig config){
        this.config = (NewsClusterQueryTransformerConfig) config;
    }

    /**
     *
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

    /**
     *
     * @param parameters
     * @return
     */
    public String getQueryFilter(final ParametersDataObject parameters) {
        StringBuilder filter = new StringBuilder();
        for (String paramField : config.getParamFields()) {
            StringDataObject paramValue = parameters.getValue(paramField);
            LOG.debug("Adding param from datamodel: " + paramField + "=" + paramValue);
            if (paramValue != null) {
                filter.append(paramField).append(':');
                filter.append('\"').append(paramValue.getString()).append('\"');
                filter.append(" and ");
            }
        }
        StringDataObject clusterId = parameters.getValue(config.getClusterIdField());
        if (clusterId != null) {
            filter.append(config.getClusterField()).append(":").append('\"').append(clusterId.getString()).append("\" ");
        } else {
            filter.append(config.getClusterField()).append(":range(1,max) ");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_WEEK, -config.getMaxAgeInDays());
        filter.append("and ").append(config.getTimestampField()).append(':');
        filter.append("range(").append(sdf.format(calendar.getTime())).append(",max)");
        return filter.toString();
    }


}
