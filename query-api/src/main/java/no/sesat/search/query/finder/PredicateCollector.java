/* Copyright (2005-2008) Schibsted Søk AS
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
 * PredicateCollector.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.sesat.search.query.finder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.BinaryOperatorClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.AbstractReflectionVisitor;
import no.sesat.search.query.Clause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.token.TokenPredicate;

/**
 * Responsible for Visiting the Query and finding where the predicate lies.
 * This class is thread-safe. (It is immutable).
 * TODO Handle predicates under NOT and ANDNOT clauses. Currently they are ignored.
 *
 *
 * @version $Id$
 */
public final class PredicateCollector extends AbstractReflectionVisitor implements Serializable {

    private final Set<TokenPredicate> known = new HashSet<TokenPredicate>();
    private final Set<TokenPredicate> possible = new HashSet<TokenPredicate>();

    /**
     *
     * @param query
     */
    public PredicateCollector(final Query query){
        visit(query.getRootClause());
    }

    /**
     *
     * @return
     */
    public Set<TokenPredicate> getKnownPredicates(){
        return known;
    }

    /**
     *
     * @return
     */
    public Set<TokenPredicate> getPossiblePredicates(){
        return possible;
    }



    /** TODO comment me. **/
    protected void visitImpl(final BinaryOperatorClause clause) {

        clause.getFirstClause().accept(this);
        collect(clause);
        clause.getSecondClause().accept(this);
    }

    /** TODO comment me. **/
    protected void visitImpl(final XorClause clause) {

        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /** TODO comment me. **/
    protected void visitImpl(final NotClause clause) {}

    /** TODO comment me. **/
    protected void visitImpl(final AndNotClause clause) {}

    /** TODO comment me. **/
    protected void visitImpl(final Clause clause) {

        collect(clause);
    }

    /** Collect predicates from this clause
     * @param clause collecting from
     */
    private void collect(final Clause clause) {

        known.addAll(clause.getKnownPredicates());
        possible.addAll(clause.getPossiblePredicates());

    }

}
