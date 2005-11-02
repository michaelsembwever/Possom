package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class AlwaysTrueTokenEvaluator implements TokenEvaluator {

    Log log = LogFactory.getLog(AlwaysTrueTokenEvaluator.class);

    public boolean evaluateToken(String token, String query) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: evaluateToken()");
        }
        return true;
    }
}
