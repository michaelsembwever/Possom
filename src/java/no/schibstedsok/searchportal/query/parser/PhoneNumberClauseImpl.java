/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.query.PhoneNumberClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.site.Site;

/**
 *
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class PhoneNumberClauseImpl extends AbstractLeafClause implements PhoneNumberClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,Map<String,WeakReference<PhoneNumberClauseImpl>>> WEAK_CACHE
            = new HashMap<Site,Map<String,WeakReference<PhoneNumberClauseImpl>>>();

    /* A IntegerClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    static {
        final Collection<TokenPredicate> predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators
        predicates.add(TokenPredicate.PHONENUMBER);
        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }


    /**
     * Creator method for PhoneNumberClauseImpl objects. By avoiding the constructors,
     * and assuming all PhoneNumberClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the PhoneNumberClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     *
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a PhoneNumberClauseImpl instance matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static PhoneNumberClauseImpl createPhoneNumberClause(
            final String term,
            final String field,
            final TokenEvaluationEngine predicate2evaluatorFactory) {

        // this needs to be moved to a query transformer as it is collection (country) dependant.
        final String t = term.length() > 8 ? term.replaceFirst("^(\\+|00)?47","") : term;

        // the weakCache to use.
        Map<String,WeakReference<PhoneNumberClauseImpl>> weakCache
                = WEAK_CACHE.get(predicate2evaluatorFactory.getSite());
        
        if(weakCache == null){
            weakCache = new HashMap<String,WeakReference<PhoneNumberClauseImpl>>();
            WEAK_CACHE.put(predicate2evaluatorFactory.getSite(),weakCache);
        }

        // use helper method from AbstractLeafClause
        return createClause(
                PhoneNumberClauseImpl.class,
                t,
                field,
                predicate2evaluatorFactory,
                PREDICATES_APPLICABLE, weakCache);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param field the field for this clause. <b>May be <code>null</code></b>.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected PhoneNumberClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, field, knownPredicates, possiblePredicates);
    }

}
