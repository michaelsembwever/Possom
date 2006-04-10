// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;

import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class AlwaysTrueTokenEvaluator implements TokenEvaluator {

    private static final Logger LOG = Logger.getLogger(AlwaysTrueTokenEvaluator.class);

    /**
     * Evaluates to true.
     *
     * @param   token   The token to look for.
     * @param   query   The query string to look in.
     *
     * @return  true.
     *
     */
    public boolean evaluateToken(final String token, final String term, final String query) {

        LOG.trace("evaluateToken()");

        return true;
    }

    public boolean isQueryDependant(TokenPredicate predicate) {
        return false;
    }
}
