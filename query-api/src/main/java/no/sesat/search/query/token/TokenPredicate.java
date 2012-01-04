/* Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.token;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.commons.collections.Predicate;

/** A Categorisation of knowledge that attaches itself
 *   as meta-data to words and groups of words (clauses) within a query. <br/><br/>
 *
 * <b>
 * Requirement that TokenPredicate, of any implementation, have a unique name.
 *</b>
 *
 *  <br/><br/>
 * Implementation of org.apache.commons.collections.Predicate for the terms in the Query.
 * Predicates use TokenEvaluators to prove the Predicate's validity to the Query.
 *
 *
 *
 * @version <tt>$Id$</tt>
 */
public interface TokenPredicate extends Predicate, Serializable{

    /** Exact (^.*$) TokenPredicates are expected to have the following prefix to their names. **/
    static final String EXACT_PREFIX = "EXACT_";

    /** The name of the TokenPredicate. Must be uppercase. Must be unique across all skins.
     *
     * @return TokenPredicate name.
     */
    String name();

    /**
     * Evaluates to true if fastListName occurs in the query. This method uses a
     * TokenEvaluationEngine to get a TokenEvaluator.
     *
     * <b>This method can only be called from the RunningQuery thread, not spawned search commands.</b>
     *
     * @param evalFactory
     *            The TTokenEvaluationEngineused to get a TokenEvaluator for
     *            this fastListName, AND to get the current term in the query being tokenised.
     * @return true if, according to the TokenEvaluator provided by the
     *         TokTokenEvaluationEngineastListName evaluates to true.
     */
    boolean evaluate(Object evalFactory);

    /** A token predicate that requires an exact match against the whole query.
     * A exact peer instance must return itself.
     *
     * @return
     */
    TokenPredicate exactPeer();

}