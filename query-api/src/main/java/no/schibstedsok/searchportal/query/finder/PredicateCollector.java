/* Copyright (2005-2007) Schibsted Søk AS
 *
 * PredicateCollector.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.schibstedsok.searchportal.query.finder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.Query;
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
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: Scorer.java 3518 2006-09-07 12:13:10Z mickw $
 */
public final class PredicateCollector extends AbstractReflectionVisitor {
    
    private static final Logger LOG = Logger.getLogger(PredicateCollector.class);
    
    private static final String ERR_ILLEGAL_TO_CALL_VISIT = "Illegal to call visit on this visitor.";
    
    private final Query query;
    private TokenEvaluationEngine engine;

    private final Set<TokenPredicate> known = new HashSet<TokenPredicate>();
    private final Set<TokenPredicate> possible = new HashSet<TokenPredicate>();
    
    public PredicateCollector(final Query query){
        this.query = query;
        visit(query.getRootClause());
    }
    
    public Set<TokenPredicate> getKnownPredicates(){
        return known;
    }
    
    public Set<TokenPredicate> getPossiblePredicates(){
        return possible;
    }
    
    

    /** TODO comment me. **/
    protected void visitImpl(final DoubleOperatorClause clause) {
        
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
