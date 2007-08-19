/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * PredicateFinder.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.sesat.searchportal.query.finder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import no.sesat.searchportal.query.AndNotClause;
import no.sesat.searchportal.query.DoubleOperatorClause;
import no.sesat.searchportal.query.XorClause;
import no.sesat.searchportal.query.parser.AbstractReflectionVisitor;
import no.sesat.searchportal.query.Clause;
import no.sesat.searchportal.query.NotClause;
import no.sesat.searchportal.query.token.TokenEvaluationEngine;
import no.sesat.searchportal.query.token.TokenPredicate;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

/**
 * Responsible for Visiting the Query and finding where the predicate lies.
 * This class is thread-safe.
 * TODO Handle predicates under NOT and ANDNOT clauses. Currently they are ignored.
 * 
 * @author <a hrefPredicateFinderto:mick@wever.org">Michael Semb Wever</a>
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
