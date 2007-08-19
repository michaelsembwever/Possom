/*
 * EvaluationState.java
 *
 * Created on September 9, 2006, 1:12 PM
 *
 */

package no.sesat.searchportal.query.token;

import java.util.Set;
import no.sesat.searchportal.query.Clause;
import no.sesat.searchportal.query.Query;

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
