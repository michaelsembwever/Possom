// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TvQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
public final class TvQueryTransformer extends AbstractQueryTransformer {

    private static final Log LOG = LogFactory.getLog(TvQueryTransformer.class);
    
    private static final int REG_EXP_OPTIONS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern TV_PATTERNS
            = Pattern.compile("((p(å|aa?)\\s+)?tv(\\s+i\\s?dag)?|(tv(-|\\s+))?program(oversikt)?|fjernsyn)(\\s*\\:)?",
                REG_EXP_OPTIONS);

    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    public String getTransformedQuery() {

        // If a channel has been chosen using tv_ syntax. Query should be empty
        // to return everything.
        if (getContext().getQuery().getQueryString().startsWith("tv_")) {
            return "";
        }
        
        final String transformedQuery = getContext().getTransformedQuery();
        return TV_PATTERNS.matcher(transformedQuery).replaceAll("");
    }


    /**
     * Set docdatetime > current date
     * @return docdatetime:>[FORMATTED DATE]
     */
    public String getFilter() {

        final String origQuery = getContext().getQuery().getQueryString();

        final StringBuilder filter = new StringBuilder();
        
        // Special case to choose channel. Mobile only.
        // TODO: we're now using tv data from two different indices. The new
        // is used by web & and the old one is used by mobile.
        if (origQuery.startsWith("tv_")) {
           filter.append("+wpfornavn:^");
           filter.append(origQuery.substring(3));
           filter.append("$ ");
           filter.append("+tvstarttime:>");
           filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));
           return filter.toString();
        } else {

            filter.append("+starttime:>");
            filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));
        
            return filter.toString();
        }
    }
}
