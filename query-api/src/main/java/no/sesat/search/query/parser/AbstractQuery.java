/* Copyright (2005-2007) Schibsted Søk AS
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
 *
 * AbstractQuery.java
 *
 * Created on 12 January 2006, 09:50
 *
 */

package no.sesat.search.query.parser;

import java.util.Set;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.finder.Counter;
import no.sesat.search.query.finder.FirstLeafFinder;
import no.sesat.search.query.finder.ParentFinder;
import no.sesat.search.query.finder.PredicateCollector;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;


/** Abstract helper for implementing a Query class.
 * Handles input of the query string and finding the first leaf clause (term) in the clause hierarchy.
 * Is thread safe. No methods return null.
 *
 * @version $Id$
 *
 */
public abstract class AbstractQuery implements Query {

    private final FirstLeafFinder finder = new FirstLeafFinder();
    private final Counter counter = new Counter();
    private final PredicateCollector predicateCollector;

    private final TokenEvaluationEngine.State evaluationState;

    private final String queryStr;

    public static Query createQuery(final String string, final boolean blank, final Clause rootClause, final ParentFinder parentFinder) {
        return new AbstractQuery(string) {

            public Clause getRootClause() {
                return rootClause;
            }

            public ParentFinder getParentFinder() {
                return parentFinder;
            }

            @Override
            public boolean isBlank() {
                return blank;
            }
        };
    }
    /** Creates a new instance of AbstractQuery .
     * @param queryStr the query string as inputted from the user.
     */
    protected AbstractQuery(final String queryStr) {

        this.queryStr = queryStr;
        predicateCollector  = new PredicateCollector(this);
        evaluationState = new TokenEvaluationEngine.State(){
            public String getTerm() {
                return null;
            }

            public Query getQuery() {
                return AbstractQuery.this;
            }

            public Set<TokenPredicate> getKnownPredicates() {
                return predicateCollector.getKnownPredicates();
            }

            public Set<TokenPredicate> getPossiblePredicates() {
                return predicateCollector.getPossiblePredicates();
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    public String getQueryString() {
        return queryStr;
    }

    /**
     * {@inheritDoc}
     */
    public LeafClause getFirstLeafClause() {
        return finder.getFirstLeaf(getRootClause());
    }

    /** TODO comment me. **/
    public int getTermCount() {
        return counter.getTermCount(getRootClause());
    }

    /** TODO comment me. **/
    public boolean isBlank(){
        return false;
    }

    public TokenEvaluationEngine.State getEvaluationState(){
        return evaluationState;
    }
}
