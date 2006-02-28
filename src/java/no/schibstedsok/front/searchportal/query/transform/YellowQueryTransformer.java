/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.query.transform;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class YellowQueryTransformer extends AbstractQueryTransformer {

    private static Log log = LogFactory.getLog(YellowQueryTransformer.class);

    public String getTransformedQuery(final Context cxt) {

        final String originalQuery = cxt.getTransformedQuery();


        final String newQuery = WhiteQueryTransformer.prefixTerms("yellowphon", "yellowpages", originalQuery);

        if (log.isDebugEnabled()) {
            log.debug("Rewriting query " + originalQuery + " to " + newQuery);
        }

        return newQuery;
    }
}
