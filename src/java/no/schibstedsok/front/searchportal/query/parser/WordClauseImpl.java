/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.front.searchportal.query.WordClause;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
/**
 * Represent a word in the query. May contain the optional field (field:word).
 * May contain both character and digits but cannot contain only digits
 * (a IntegerClause will be used instead then).
 * 
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class WordClauseImpl extends AbstractLeafClause implements WordClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<String,WeakReference<WordClauseImpl>> WEAK_CACHE = new HashMap<String,WeakReference<WordClauseImpl>>();

    /* A WordClauseImpl specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    static {
        final Collection<TokenPredicate> predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators
        predicates.addAll(TokenPredicate.getMagicTokenPredicates());
        predicates.addAll(TokenPredicate.getTriggerTokenPredicates());


        predicates.add(TokenPredicate.COMPANYSUFFIX);
        predicates.add(TokenPredicate.ORGNR);
        predicates.add(TokenPredicate.SITEPREFIX);        

        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    /**
     * Creator method for WordClauseImpl objects. By avoiding the constructors,
     * and assuming all WordClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the WordClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     * 
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a WordClauseImpl instance matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static WordClauseImpl createWordClause(
            final String term,
            final String field,
            final TokenEvaluatorFactory predicate2evaluatorFactory) {

        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);
        // use helper method from AbstractLeafClause
        return createClause(
                WordClauseImpl.class,
                term,
                field,
                predicate2evaluatorFactory,
                PREDICATES_APPLICABLE, WEAK_CACHE);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param field the field for this clause. <b>May be <code>null</code></b>.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected WordClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, field, knownPredicates, possiblePredicates);

    }


}
