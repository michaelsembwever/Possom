/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.query.token;



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
     * other possibilities such as tokens that always evaluates to true.
     *
     * @param term     the token to look for.
     * @return true if token occurs in query
     */
    boolean evaluateToken(TokenPredicate token, String term, String query);

    boolean isQueryDependant(TokenPredicate predicate);
}
