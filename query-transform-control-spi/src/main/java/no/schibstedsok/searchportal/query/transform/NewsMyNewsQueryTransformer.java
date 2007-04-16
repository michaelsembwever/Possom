package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.Clause;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Expects a parameter to be on the format: ((&lt;value&gt;::&lt;type&gt;)(||&lt;value&gt;::&lt;type&gt;)*)?
 * <p/>
 * It the type matches the type for this transformer, it will use the value(s) to transform to a new query.
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. All transforms to the resulting
 * query should be done after this.
 */
public class NewsMyNewsQueryTransformer extends AbstractQueryTransformer {
    private static final Logger LOG = Logger.getLogger(NewsMyNewsQueryTransformer.class);
    private static final Pattern queryPattern = Pattern.compile("(?:\\A|\\|)([^\\|]+)\\:{2}([^\\|]+)\\|?");

    private NewsMyNewsQueryTransformerConfig config;

    public NewsMyNewsQueryTransformer(QueryTransformerConfig config) {
        this.config = (NewsMyNewsQueryTransformerConfig) config;
    }

    public void visitImpl(final Clause clause) {
        String myNews = getContext().getQuery().getQueryString();
        LOG.debug("Transforming query according to query = " + myNews);
        final String transformedQuery = transformQuery(myNews);
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

    private String transformQuery(String myNews) {
        if (myNews != null && myNews.length() > 0) {
            StringBuilder newQuery = new StringBuilder();
            Matcher matcher = queryPattern.matcher(myNews);
            if (config.getPosition() == -1) {
                while (matcher.find()) {
                    if (matcher.group(2).equals(config.getType())) {
                        if (newQuery.length() == 0) {
                            newQuery.append("filter(").append(config.getFilterField()).append(":or(");
                        } else {
                            newQuery.append(',');
                        }
                        newQuery.append('\"').append(matcher.group(1)).append('\"');
                    }
                }
            } else {
                int curPos = 0;
                while (matcher.find() && curPos < config.getPosition()) {
                    // Just searching for the correct match.
                    curPos++;
                }
                if (matcher.group(2).equals(config.getType()) && matcher.groupCount() > 0) {
                    newQuery.append("filter(").append(config.getFilterField()).append('(').append('\"').append(matcher.group(1)).append('\"');
                } else {
                    return "";
                }
            }
            if (newQuery.length() > 0) {
                newQuery.append("))");
                return newQuery.toString();
            }
        }
        return "";
    }
}
