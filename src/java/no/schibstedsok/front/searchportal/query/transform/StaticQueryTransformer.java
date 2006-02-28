// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Transform query by appending keywords
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 *
 */
public class StaticQueryTransformer extends AbstractQueryTransformer {

    private static Log log = LogFactory.getLog(StaticQueryTransformer.class);

    private String staticAddition;

    /**
     * The string that is to be appended to the query
     * @param staticAddition the string that is added
     */
    public StaticQueryTransformer(final Context cxt) {

        final String originalQuery = cxt.getTransformedQuery();

        this.staticAddition = staticAddition;
    }

    /**
     * Get the query after its transformed
     * @param originalQuery that should be appended to
     * @return transformed query
     */
    public String getTransformedQuery(final Context cxt) {

        final String originalQuery = cxt.getTransformedQuery();

        final String newQuery = originalQuery + " " + staticAddition;

        if (log.isDebugEnabled()) {
            log.debug("Rewriting query " + originalQuery + " to " + newQuery);
        }

        return newQuery;
    }
}
