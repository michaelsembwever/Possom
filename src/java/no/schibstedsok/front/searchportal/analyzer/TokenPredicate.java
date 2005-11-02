package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.collections.Predicate;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TokenPredicate implements Predicate {

    private String token;

    public TokenPredicate(String token) {
        this.token = token;
    }

    public boolean evaluate(Object object) {
        TokenEvaluatorFactory factory = (TokenEvaluatorFactory) object;
        return factory.getEvaluator(token).evaluateToken(token, factory.getQuery());
    }
}
