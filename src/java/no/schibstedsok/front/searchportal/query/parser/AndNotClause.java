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
public final class AndNotClause extends AbstractOperationClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map/*<Long,WeakReference<AndNotClause>>*/ WEAK_CACHE = new HashMap/*<Long,WeakReference<AndNotClause>>*/();

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

    public static AndNotClause createAndNotClause(
        final LeafClause first,
        final Clause second,
        final TokenEvaluatorFactory predicate2evaluatorFactory) {

        // construct the proper "schibstedsøk" formatted term for this operation.
        //  XXX eventually it would be nice not to have to expose the internal string representation of this object.
        final String term = (first.getField() != null ? first.getField() + ":" : "")
                + first.getTerm()
                + " ANDNOT "
                + ( second instanceof LeafClause && ((LeafClause) second).getField() != null
                    ?  ((LeafClause) second).getField() + ":"
                    : "")
                + second.getTerm();

        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);

        // use helper method from AbstractLeafClause
        return (AndNotClause) createClause(
                AndNotClause.class,
                term,
                first,
                second,
                predicate2evaluatorFactory,
                PREDICATES_APPLICABLE, WEAK_CACHE);
    }

    /**
     *
     * @param first
     * @param second
     */
    protected AndNotClause(
            final String term,
            final Clause first,
            final Clause second,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {

        super(term, knownPredicates, possiblePredicates);
        this.firstClause = first;
        this.secondClause = second;
    }

    /**
     *
     * @param visitor
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
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
