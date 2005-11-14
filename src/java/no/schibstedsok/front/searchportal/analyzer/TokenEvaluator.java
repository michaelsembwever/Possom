/*
 * Copyright (2005) Schibsted S¿k AS
 */
package no.schibstedsok.front.searchportal.analyzer;

/**
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface TokenEvaluator {

    /**
     *
     * Evaluate the <code>token</code> with regards to <code>query</code>.
     * This usually means to check if the token occurs in query, but there are
     * other possiblities such as tokens that always evaluates to true.
     *
     * @param token     the token to look for.
     * @param query     the query to look in.
     * @return true if token occurs in query
     */
    boolean evaluateToken(String token, String query);
}
