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
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

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
    private static final Map/*<Long,WeakReference<AndNotClauseImpl>>*/ WEAK_CACHE = new HashMap/*<Long,WeakReference<AndNotClauseImpl>>*/();

    /* A WordClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection/*<Predicate>*/ PREDICATES_APPLICABLE;

    static {
        final Collection/*<Predicate>*/ predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators

        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    private final Clause firstClause;
    private final Clause secondClause;

    /**
     * Creator method for AndNotClauseImpl objects. By avoiding the constructors,
     * and assuming all AndNotClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the AndNotClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     * 
     * @param first the left child clause of the operation clause we are about to create (or find).
     * The current implementation always creates a right-leaning query heirarchy.
     * Therefore the left child clause to any operation clause must be a LeafClause.
     * @param second the right child clause of the operation clause we are about to create (or find).
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a AndAndNotClauseImplstance matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static AndNotClauseImpl createAndNotClause(
        final LeafClause first,
        final Clause second,
        final TokenEvaluatorFactory predicate2evaluatorFactory) {

        // construct the proper "schibstedsøk" formatted term for this operation.
        //  XXX eventually it would be nice not to have to expose the internal string representation of this object.
        final String term = (first.getField() != null ? first.getField() + ":" : "")
                + first.getTerm()
                + " ANDNOT "
                + (second instanceof LeafClause && ((LeafClause) second).getField() != null
                    ?  ((LeafClause) second).getField() + ":"
                    : "")
                + second.getTerm();

        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);

        // use helper method from AbstractLeafClause
        return (AndNotClauseImpl) createClause(
                AndNotClauseImpl.class,
                term,
                first,
                second,
                predicate2evaluatorFactory,
                PREDICATES_APPLICABLE, WEAK_CACHE);
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
            final Clause first,  // really is a LeafClause
            final Clause second,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {

        super(term, knownPredicates, possiblePredicates);
        this.firstClause = first;
        this.secondClause = second;
    }
    /**
     * Get the firstClause.
     *
     * @return the firstClause.
     */
    public Clause getFirstClause() {
        return firstClause;
    }

    /**
     * Get the secondClause.
     *
     * @return the secondClause.
     */
    public Clause getSecondClause() {
        return secondClause;
    }

}
