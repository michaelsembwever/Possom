package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class AlwaysTrueTokenEvaluator implements TokenEvaluator {

    private static Log log = LogFactory.getLog(AlwaysTrueTokenEvaluator.class);

    /**
     * Evaluates to true.
     *
     * @param   token   The token to look for.
     * @param   query   The query string to look in.
     *
     * @return  true.
     *
     */
    public boolean evaluateToken(final String token, final String query) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: evaluateToken()");
        }
        return true;
    }
}
