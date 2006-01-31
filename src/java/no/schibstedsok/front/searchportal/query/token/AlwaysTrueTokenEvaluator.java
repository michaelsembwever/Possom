// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class AlwaysTrueTokenEvaluator implements TokenEvaluator {

    private static final Log LOG = LogFactory.getLog(AlwaysTrueTokenEvaluator.class);

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

        if (LOG.isDebugEnabled()) { LOG.debug("ENTR: evaluateToken()");  }

        return true;
    }

    public boolean isQueryDependant() {
        return false;
    }
}
