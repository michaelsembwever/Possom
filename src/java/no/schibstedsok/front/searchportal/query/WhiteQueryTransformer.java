/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class WhiteQueryTransformer extends AbstractQueryTransformer {

    private static Pattern numbers = Pattern.compile("^\\d+$");

    private static Log log = LogFactory.getLog(WhiteQueryTransformer.class);

    public String getTransformedQuery(final Context cxt) {
        
        final String originalQuery = cxt.getQueryString();
        String newQuery = prefixTerms("whitephon", "whitepages", originalQuery);

        if (log.isDebugEnabled()) {
            log.debug("Rewriting query " + originalQuery + " to " + newQuery);
        }

        return newQuery;
    }

    public static String prefixTerms(final String prefix, final String numberPrefix, final String query) {

        String stripped = query.replaceAll("\"", "");

        String[] tokens = stripped.split("\\s");

        StringBuffer newQuery = new StringBuffer();

        for (int i = 0; i < tokens.length; i++) {

            Matcher m = numbers.matcher(tokens[i]);

            if (m.find()) {
                newQuery.append(numberPrefix).append(":");
            } else {
                newQuery.append(prefix).append(":");
            }

            newQuery.append(tokens[i]);

            if (i < tokens.length - 1) {
                newQuery.append(" ");
            }
        }

        return newQuery.toString();
    }
}
