/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenPredicate;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class PhraseClause extends WordClause {

    /** Values are WeakReference object to AbstractClause. 
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map/*<Long,WeakReference<AbstractClause>>*/ WEAK_CACHE = new HashMap/*<Long,WeakReference<AbstractClause>>*/();
    
    /* A WordClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection/*<Predicate>*/ PREDICATES_APPLICABLE; // TokenPredicate.getTokenPredicates();
    
    static{
        final Collection/*<Predicate>*/ predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators
        predicates.add(TokenPredicate.PICTUREPREFIX);
        predicates.add(TokenPredicate.NEWSPREFIX);
        predicates.add(TokenPredicate.WIKIPEDIAPREFIX);
        predicates.add(TokenPredicate.TVPREFIX);
        predicates.add(TokenPredicate.COMPANYSUFFIX);
        predicates.add(TokenPredicate.WEATHERPREFIX);
        predicates.add(TokenPredicate.MATHPREDICATE);
        predicates.add(TokenPredicate.CATALOGUEPREFIX);
        predicates.add(TokenPredicate.ORGNR);
        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }
    
    public static PhraseClause createPhraseClause(
            final String term, 
            final String field,
            final TokenEvaluatorFactory predicate2evaluatorFactory) {
        
        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);
        // use helper method from AbstractLeafClause
        return (PhraseClause)createClause(
                PhraseClause.class, 
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
    protected PhraseClause(
            final String term, 
            final String field,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {
        
        super(term, field, knownPredicates, possiblePredicates);
        
    }
}
