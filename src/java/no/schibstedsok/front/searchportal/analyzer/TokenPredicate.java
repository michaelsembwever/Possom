package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.collections.Predicate;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TokenPredicate implements Predicate {

    private String token;

    /**
     * Create a new TokenPredicate that will return true iff token occurs in the
     * query.
     *
     * @param token     the token.
     */
    public TokenPredicate(final String token) {
        this.token = token;
    }

    /**
     * Evaluates to true iff token occurs in the query. This method uses a
     * TokenEvaluatorFactory to get a TokenEvaluator
     *
     * @param evalFactory
     *            The TokenEvaluatorFactory used to get a TokenEvaluator for
     *            this token.
     *
     * @return true if, according to the TokenEvaluator provided by the
     *         TokenEvaluatorFactory, token evaluates to true.
     */
    public boolean evaluate(final Object evalFactory) {
        TokenEvaluatorFactory factory = (TokenEvaluatorFactory) evalFactory;
        String query = factory.getQuery();
        return factory.getEvaluator(token).evaluateToken(token, query);
    }
}
