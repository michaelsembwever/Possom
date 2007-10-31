/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.parser;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import no.sesat.commons.ref.ReferenceMap;
import no.sesat.search.query.PhoneNumberClause;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.site.Site;

/**
 *
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class PhoneNumberClauseImpl extends AbstractLeafClause implements PhoneNumberClause {

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,ReferenceMap<String,PhoneNumberClauseImpl>> WEAK_CACHE
            = new ConcurrentHashMap<Site,ReferenceMap<String,PhoneNumberClauseImpl>>();

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

        // the weakCache to use.
        ReferenceMap<String,PhoneNumberClauseImpl> weakCache
                = WEAK_CACHE.get(predicate2evaluatorFactory.getSite());

        if(weakCache == null){

            weakCache = new ReferenceMap<String,PhoneNumberClauseImpl>(
                    ReferenceMap.Type.WEAK,
                    new ConcurrentHashMap<String,Reference<PhoneNumberClauseImpl>>(
                        WEAK_CACHE_INITIAL_CAPACITY,
                        WEAK_CACHE_LOAD_FACTOR,
                        WEAK_CACHE_CONCURRENCY_LEVEL));

            WEAK_CACHE.put(predicate2evaluatorFactory.getSite(), weakCache);
        }

        // use helper method from AbstractLeafClause
        return createClause(
                PhoneNumberClauseImpl.class,
                term,
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
