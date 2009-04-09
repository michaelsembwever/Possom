/*
 * Copyright (2005-2008) Schibsted ASA
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.token;
import java.util.Set;



/** TokenEvaluator works behind a TokenPredicate doing the actual work to prove the predicate true or false.
 * This work is the evaluating process. Each TokenValuator is constructed and obtained through a corresponding
 * AbstractEvaluatorFactory.
 *
 * evaluateToken(TokenPredicate, String, String) is the primary method in the class,
 * but evaluators may also indicate whether the position of words within the query influences evaluation
 * by the isQueryDependant(TokenPredicate) method.
 *
 * And synonyms may also be provided, extending predicates from true|false to being a richer meta data source.
 *
 *
 * @version <tt>$Id$</tt>
 */
public interface TokenEvaluator {

    /**
     *
     * Evaluate the <code>token</code> with regards to <code>query</code>.
     * This usually means to check if the token occurs in query, but there are
     * other possibilities such as tokens that always evaluates to true.
     *
     * @param token
     * @param term     the token to look for.
     * @param query
     * @return true if token occurs in query
     */
    boolean evaluateToken(TokenPredicate token, String term, String query);

    /**
     *
     * @param predicate
     * @return
     */
    boolean isQueryDependant(TokenPredicate predicate);

    /** Each true evaluation may also provide a synonym (or "match value").
     *
     * @param token
     * @param term
     * @return a list of Tokens
     */
    Set<String> getMatchValues(final TokenPredicate token, final String term);
}
