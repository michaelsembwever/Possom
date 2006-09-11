/* Copyright (2005-2006) Schibsted Søk AS
 *
 * PredicateFinder.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.schibstedsok.searchportal.query.finder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

/**
 * Responsible for Visiting the Query and finding where the predicate lies.
 * This class is thread-safe.
 * TODO Handle predicates under NOT and ANDNOT clauses. Currently they are ignored.
 * 
 * @author <a hrefPredicateFinderto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: Scorer.java 3518 2006-09-07 12:13:10Z mickw $
 */
public abstract class PredicateFinder extends AbstractReflectionVisitor {

    private static final Logger LOG = Logger.getLogger(PredicateFinder.class);
    
    private Predicate predicate;
    private TokenEvaluationEngine engine;
    private boolean singleMode = false;

    private final Set<Clause> clauses = new HashSet<Clause>();
    private Clause clause;


    /** find the first clause containing the predicate.
     **/
    public synchronized Clause findFirstClause(
            final Clause root, 
            final Predicate predicate, 
            final TokenEvaluationEngine engine) {
        
        singleMode = true;
        findImpl(root, predicate, engine);  
        singleMode = false;
        return clause;
    }
    
    /** find all the clauses containing the predicate.
     **/
    public synchronized Set<Clause> findClauses(
            final Clause root, 
            final Predicate predicate, 
            final TokenEvaluationEngine engine) {
        
        findImpl(root, predicate, engine);        
        return Collections.unmodifiableSet(clauses);
    }
    
    private void findImpl(
            final Clause root, 
            final Predicate predicate, 
            final TokenEvaluationEngine engine){
        
        this.predicate = predicate;
        this.engine = engine;
        clause = null;
        clauses.clear();
        
        visit(root);
    }

    /** TODO comment me. **/
    protected void visitImpl(final DoubleOperatorClause clause) {
        
        if(null == clause){
            clause.getFirstClause().accept(this);
            evaluate(clause);
            clause.getSecondClause().accept(this);
        }
    }
    
    /** TODO comment me. **/
    protected void visitImpl(final XorClause clause) {
        
        if(null == clause){
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
    }

    /** TODO comment me. **/
    protected void visitImpl(final NotClause clause) {}

    /** TODO comment me. **/
    protected void visitImpl(final AndNotClause clause) {}

    /** TODO comment me. **/
    protected void visitImpl(final Clause clause) {
        
        if(null == clause){ 
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

        // update the engine with the predicate sets that can be used to improve evaluation performance.
//        engine.setClausesKnownPredicates(knownPredicates);
//        engine.setClausesPossiblePredicates(possiblePredicates);
        

        // if this is a possiblePredicate or a all|any|none|not predicate
        //  find out if it is now applicable...
        boolean applicable = clause.getKnownPredicates().contains(predicate);
        applicable |=
                clause.getPossiblePredicates().contains(predicate) || !(predicate instanceof TokenPredicate)
                && predicate.evaluate(engine);

        if (applicable) {

            if(singleMode){
                this.clause = clause;
            }else{
                clauses.add(clause);
            }
        }

    }

}
