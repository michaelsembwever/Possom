/*
 * Copyright (2005-2007) Schibsted Søk AS
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

import java.io.Serializable;
import java.util.Set;
import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.search.query.Clause;
import no.sesat.search.query.Query;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.query.QueryStringContext;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;

/**
 * TokenEvaluationEngine contains state as to what is the current term being tokenised,
 * and the term's sets of known and possible predicates.
 * These sets can be in building process, and provide performance improvement by not having to
 * evaluate the token twice.
 *
 * A TokenEvaluationEngine also provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token {@link TokenPredicate}.
 *
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@wever.org">Mck</a>
 * @version <tt>$Revision$</tt>
 */
public interface TokenEvaluationEngine {

    public interface Context extends BaseContext, QueryStringContext, ResourceContext, SiteContext{
    }

    public interface State extends Serializable {
        /** the current clause's term, or null if in query-evaluation mode. **/
        String getTerm();
        /** the current query, or null if in term-evaluation mode. **/
        Query getQuery();
        /** known matching predicates. by making this available performance is improved. **/
        Set<TokenPredicate> getKnownPredicates();
        /** possible matching predicates. by making this available performance is improved. **/
        Set<TokenPredicate> getPossiblePredicates();
    }

    /** Find or create the TokenEvaluator that will evaluate if given (Token)Predicate is true.
     *
     * @param token
     * @return
     */
    TokenEvaluator getEvaluator(TokenPredicate token) throws VeryFastListQueryException;

    /**
     *
     *
     * @return
     */
    String getQueryString();

     /** TODO comment me. **/
    Site getSite();

    /** Utility method to perform one-off evaluations on terms from non RunningQuery threads.
     * Typically used by TokenTransformers or performing evaluations on non-clause oriented strings.
     **/
    boolean evaluateTerm(TokenPredicate predicate, String term);

    /** Utility method to perform one-off evaluations on clauses from non RunningQuery threads.
     * Typically used by TokenTransformers or performing evaluations on non-clause oriented strings.
     **/
    boolean evaluateClause(TokenPredicate predicate, Clause clause);

    /** Utility method to perform one-off evaluations on queries from non RunningQuery threads.
     * Typically used by TokenTransformers or performing evaluations on non-clause oriented strings.
     **/
    boolean evaluateQuery(TokenPredicate predicate, Query query);

    Thread getOwningThread();

    /**
     * Getter for property state.
     * @return Value of property state.
     */
    public State getState();

    /**
     * Setter for property state.
     * @param state New value of property state.
     */
    public void setState(State state);


}
