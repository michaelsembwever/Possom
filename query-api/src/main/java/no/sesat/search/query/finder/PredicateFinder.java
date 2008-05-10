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
 * PredicateFinder.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.sesat.search.query.finder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.AbstractReflectionVisitor;
import no.sesat.search.query.Clause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

/**
 * Responsible for Visiting the Query and finding where the predicate lies.
 * This class is thread-safe.
 * TODO Handle predicates under NOT and ANDNOT clauses. Currently they are ignored.
 *
 *
 * @version $Id$
 */
public final class PredicateFinder extends AbstractReflectionVisitor {

    private static final Logger LOG = Logger.getLogger(PredicateFinder.class);

    private Predicate predicate;
    private TokenEvaluationEngine engine;
    private boolean singleMode = false;

    private final Set<Clause> clauses = new HashSet<Clause>();
    private Clause firstClause;


    /** find the first clause containing the predicate.
     ** @param root
     * @param predicate
     * @param engine
     * @return
     */
    public synchronized Clause findFirstClause(
            final Clause root,
            final Predicate predicate,
            final TokenEvaluationEngine engine) {

        singleMode = true;
        findImpl(root, predicate, engine);
        singleMode = false;
        return firstClause;
    }

    /** find all the clauses containing the predicate.
     * returns largest multi-terms clauses and before leaf clauses.
     * @param root
     * @param predicate
     * @param engine
     * @return
     **/
    public synchronized Set<Clause> findClauses(
            final Clause root,
            final Predicate predicate,
            final TokenEvaluationEngine engine) {

        findImpl(root, predicate, engine);
        return Collections.unmodifiableSet(new HashSet<Clause>(clauses));
    }

    private void findImpl(
            final Clause root,
            final Predicate predicate,
            final TokenEvaluationEngine engine){

        this.predicate = predicate;
        this.engine = engine;
        firstClause = null;
        clauses.clear();

        visit(root);
    }

    /** TODO comment me. *
     * @param clause
     */
    protected void visitImpl(final DoubleOperatorClause clause) {

        if(null == firstClause){
            evaluate(clause);
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
    }

    /** TODO comment me. *
     * @param clause
     */
    protected void visitImpl(final XorClause clause) {

        if(null == firstClause){
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
    }

    /** TODO comment me. *
     * @param clause
     */
    protected void visitImpl(final NotClause clause) {}

    /** TODO comment me. *
     * @param clause
     */
    protected void visitImpl(final AndNotClause clause) {}

    /** TODO comment me. *
     * @param clause
     */
    protected void visitImpl(final Clause clause) {

        if(null == firstClause){
            evaluate(clause);
        }
    }

    /** Find if this clause contains (either known, possible, or custom joined) predicates corresponding to
     * PredicateScores listed in the context.
     * @param the clause we are scoring.
     * @param addition whether the score will be added or subtracted.
     */
    private void evaluate(final Clause clause) {

        final Set<TokenPredicate> knownPredicates = clause.getKnownPredicates();
        final Set<TokenPredicate> possiblePredicates = clause.getPossiblePredicates();

        // if this is a possiblePredicate or a all|any|none|not predicate
        //  find out if it is now applicable...
        boolean applicable = knownPredicates.contains(predicate);
        applicable |=
                possiblePredicates.contains(predicate) || !(predicate instanceof TokenPredicate)
                && predicate.evaluate(engine);

        if (applicable) {

            if(singleMode){
                firstClause = clause;
            }else{
                clauses.add(clause);
            }
        }

    }

}
