/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * PredicateCollector.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.schibstedsok.searchportal.query.finder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.token.TokenPredicate;

/**
 * Responsible for Visiting the Query and finding where the predicate lies.
 * This class is thread-safe. (It is immutable).
 * TODO Handle predicates under NOT and ANDNOT clauses. Currently they are ignored.
 * 
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
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
