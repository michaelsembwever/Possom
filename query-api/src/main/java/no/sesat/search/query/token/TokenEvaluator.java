/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
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
