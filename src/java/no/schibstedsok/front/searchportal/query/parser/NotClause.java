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
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenPredicate;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class NotClause extends AbstractOperationClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map/*<Long,WeakReference<NotClause>>*/ WEAK_CACHE = new HashMap/*<Long,WeakReference<NotClause>>*/();

    /* A WordClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection/*<Predicate>*/ PREDICATES_APPLICABLE;

    static {
        final Collection/*<Predicate>*/ predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);

        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    private final Clause clause;

    public static NotClause createNotClause(
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
        return (NotClause) createClause(
                NotClause.class,
                term,
                first,
                null,
                predicate2evaluatorFactory,
                PREDICATES_APPLICABLE, WEAK_CACHE);
    }

    /**
     *
     * @param clause
     */
    protected NotClause(
            final String term,
            final Clause first,  // really is a LeafClause
            final Clause second,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {

        super(term, knownPredicates, possiblePredicates);
        this.clause = first;
        //this.secondClause = second; // this parameter not used! On purpose. See {@link createClause}.
    }

    /**
     *
     * @return
     */
    public Clause getClause() {
        return clause;
    }

    /**
     *
     * @param visitor
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

}
