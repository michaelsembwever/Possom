/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.token.EvaluationState;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.site.Site;

/**
 * The AndNotClauseImpl represents a joining not clause between two terms in the query.
 * For example: "term1 ANDNOT term2".
 * <b>Objects of this class are immutable</b>
 * 
 * @author <a hrefAndNotClauseImplk@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class AndNotClauseImpl extends AbstractOperationClause implements AndNotClause {

    /**
     * Values are WeakReference object to AndNotClauseImpl.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     * An overlap of creation is non-critical.
     */
    private static final Map<Site,Map<String,WeakReference<AndNotClauseImpl>>> WEAK_CACHE 
            = new HashMap<Site,Map<String,WeakReference<AndNotClauseImpl>>>();
    
    /* A WordClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    static {
        final Collection<TokenPredicate> predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators

        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    /**
     * Creator method for AndNotClauseImpl objects. By avoiding the constructors,
     * and assuming all AndNotClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the AndNotClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     * 
     * 
     * @param first the left child clause of the operation clause we are about to create (or find).
     * @param second the right child clause of the operation clause we are about to create (or find).
     * @param engine the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a AndAndNotClauseImplstance matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static AndNotClauseImpl createAndNotClause(
        final Clause first,
        final TokenEvaluationEngine engine) {

        // construct the proper "schibstedsøk" formatted term for this operation.
        //  XXX eventually it would be nice not to have to expose the internal string representation of this object.
        final String term = "ANDNOT "
                + (first instanceof LeafClause && ((LeafClause) first).getField() != null
                    ?  ((LeafClause) first).getField() + ":"
                    : "")
                + first.getTerm();

        // create predicate sets
        engine.setState(new EvaluationState(term, new HashSet<TokenPredicate>(), new HashSet<TokenPredicate>()));
        
        // the weakCache to use.
        Map<String,WeakReference<AndNotClauseImpl>> weakCache = WEAK_CACHE.get(engine.getSite());
        if( weakCache == null ){
            weakCache = new HashMap<String,WeakReference<AndNotClauseImpl>>();
            WEAK_CACHE.put(engine.getSite(),weakCache);
        }

        // use helper method from AbstractLeafClause
        return createClause(
                AndNotClauseImpl.class,
                term,
                first,
                null,
                engine,
                PREDICATES_APPLICABLE, weakCache);
    }

    /**
     * Create the AndNotClauseImpl with the given term, left and right child clauses, and known and possible predicate sets.
     * 
     * @param term the term for this AndClause.
     * @param knownPredicates set of known predicates.
     * @param possiblePredicates set of possible predicates.
     * @param first the left child clause.
     * @param second the right child clause.
     */
    protected AndNotClauseImpl(
            final String term,
            final Clause first,  
            final Clause second,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, first, knownPredicates, possiblePredicates);

    }
}
