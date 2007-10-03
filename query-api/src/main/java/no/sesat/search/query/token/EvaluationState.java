/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * EvaluationState.java
 *
 * Created on September 9, 2006, 1:12 PM
 *
 */

package no.sesat.search.query.token;

import java.util.Set;
import no.sesat.search.query.Clause;
import no.sesat.search.query.Query;

/**
 *
 * @author mick
 */
public class EvaluationState implements TokenEvaluationEngine.State{
    
    private final String term; 
    private final Query query;
    private final Set<TokenPredicate> known;
    private Set<TokenPredicate> possible;    
    
    /**
     * Creates a new instance of EvaluationState
     */
    public EvaluationState(
            final String term,
            final Set<TokenPredicate> known,
            final Set<TokenPredicate> possible) {
        
        this.term = term;
        this.query = null;
        this.known = known;
        this.possible = possible;
    }
    
    public EvaluationState(final Clause clause){
        
        this.term = clause.getTerm();
        this.query = null;
        this.known = clause.getKnownPredicates();
        this.possible = clause.getPossiblePredicates();
    }

    public String getTerm() {
        return term;
    }

    public Query getQuery() {
        return query;
    }

    public Set<TokenPredicate> getKnownPredicates() {
        return known;
    }

    public Set<TokenPredicate> getPossiblePredicates() {
        return possible;
    }
    
}
