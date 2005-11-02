package no.schibstedsok.front.searchportal.analyzer;

/**
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface TokenEvaluator {

    boolean evaluateToken(String token, String query);
}
