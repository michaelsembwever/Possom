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
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;

/**
 * The NotClauseImpl represents a not clause between prefixing another term in the query.
 * For example: "NOT term1".
 * <b>Objects of this class are immutable</b>
 * 
 * @author <a hrefNotClauseImplmick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class NotClauseImpl extends AbstractOperationClause implements NotClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map/*<Long,WeakReference<NotClauseImpl>>*/ WEAK_CACHE = new HashMap/*<Long,WeakReference<NotClauseImpl>>*/();

    /* A WordClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection/*<Predicate>*/ PREDICATES_APPLICABLE;

    static {
        final Collection/*<Predicate>*/ predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);

        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    private final Clause clause;

    /**
     * Creator method for NotClauseImpl objects. By avoiding the constructors,
     * and assuming all NotClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the NotClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     * 
     * @param first the left child clause of the operation clause we are about to create (or find).
     * The current implementation always creates a right-leaning query heirarchy.
     * Therefore the left child clause to any operation clause must be a LeafClause.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a NoNotClauseImplnstance matching the term, and left child clauses.
     * May be either newly created or reused.
     */
    public static NotClauseImpl createNotClause(
        final LeafClause first,
        final TokenEvaluatorFactory predicate2evaluatorFactory) {

        // construct the proper "schibstedsøk" formatted term for this operation.
        //  XXX eventually it would be nice not to have to expose the internal string representation of this object.
        final String term = "NOT "
                + (first.getField() != null ? first.getField() + ":" : "")
                + first.getTerm();

        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);

        // use helper method from AbstractLeafClause
        return (NotClauseImpl) createClause(
                NotClauseImpl.class,
                term,
                first,
                null,
                predicate2evaluatorFactory,
                PREDICATES_APPLICABLE, WEAK_CACHE);
    }

    /**
     * Create the NotClauseImpl with the given term, and left child clauses, and known and possible predicate sets.
     * 
     * @param term the term for this OrClause.
     * @param knownPredicates set of known predicates.
     * @param possiblePredicates set of possible predicates.
     * @param first the left child clause.
     * @param second the right child clause.
     * NOT USED but required to utilitise the createClause method in createNotClause.
     */
    protected NotClauseImpl(
            final String term,
            final Clause first,  // really is a LeafClause
            final Clause second,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {

        super(term, knownPredicates, possiblePredicates);
        this.clause = first;
        //this.secondClause = second; // this parameter not used! On purpose. See {@link createClause}.
    }

    /** Get the clause.
     *
     * @return the clause.
     */
    public Clause getFirstClause() {
        return clause;
    }

}
