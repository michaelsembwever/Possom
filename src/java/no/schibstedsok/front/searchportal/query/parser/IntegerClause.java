/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenPredicate;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class IntegerClause extends WordClause {
    
    /** Values are WeakReference object to AbstractClause. 
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map/*<Long,WeakReference<AbstractClause>>*/ WEAK_CACHE = new HashMap/*<Long,WeakReference<AbstractClause>>*/();
    
    // [TOD0] this should be a WordClause specific list!    
    private static final Collection/*<Predicate>*/ PREDICATES_APPLICABLE = TokenPredicate.getTokenPredicates();
    
    
    public static IntegerClause createIntegerClause(
        final String term, 
        final String field,
        final TokenEvaluatorFactory predicate2evaluatorFactory) {
        
        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);
        // use helper method from AbstractLeafClause
        return (IntegerClause)createClause(
                IntegerClause.class, 
                term, 
                field, 
                predicate2evaluatorFactory, 
                PREDICATES_APPLICABLE, WEAK_CACHE);
    }

    /**
     *
     * @param term
     * @param field
     */
    protected IntegerClause(
            final String term, 
            final String field,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {
        
        super(term, field, knownPredicates, possiblePredicates);
    }

}
