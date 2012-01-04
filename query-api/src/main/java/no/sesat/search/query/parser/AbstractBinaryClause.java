/* Copyright (2012) Schibsted ASA
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
 *
 */

package no.sesat.search.query.parser;

import java.util.Set;
import no.sesat.search.query.Clause;
import no.sesat.search.query.token.TokenPredicate;

/**
 * Abstract binary operation clause. That is an operation between two clauses.
 *
 */
public class AbstractBinaryClause extends AbstractUnaryClause {

    private final Clause secondClause;

    /**
     * Create the infix operation clause with the given term, left and right child clauses, and known and possible predicate sets.
     *
     * @param term the term for this OrClauseImpl.
     * @param knownPredicates set of known predicates.
     * @param possiblePredicates set of possible predicates.
     * @param first the left child clause.
     * @param second the right child clause.
     */
    protected AbstractBinaryClause(
            final String term,
            final Clause first,
            final Clause second,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, first, knownPredicates, possiblePredicates);
        this.secondClause = second;
    }

    /**
     * Get the secondClause.
     *
     * @return the secondClause.
     */
    public Clause getSecondClause() {
        return secondClause;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getFirstClause() + ", " + getSecondClause() + ']';
    }
}
