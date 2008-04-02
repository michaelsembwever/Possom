/*
 * Copyright (2005-2008) Schibsted Søk AS
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import no.sesat.commons.ref.ReferenceMap;
import no.sesat.search.query.IntegerClause;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.site.Site;

/**
 * IntegerClauseImpl. Contains only digits.
 *
 * <b>Objects of this class are immutable</b>
 *
 * @author <a hrefIntegerClauseImpl@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class IntegerClauseImpl extends AbstractLeafClause implements IntegerClause {

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,ReferenceMap<String,IntegerClauseImpl>> WEAK_CACHE
            = new ConcurrentHashMap<Site,ReferenceMap<String,IntegerClauseImpl>>();

    /**
     * Creator method for IntegerClauseImpl objects. By avoiding the constructors,
     * and assuming all IntegerClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the IntegerClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     *
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a IntIntegerClauseImplstance matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static IntegerClauseImpl createIntegerClause(
        final String term,
        final String field,
        final TokenEvaluationEngine predicate2evaluatorFactory) {

        // the weakCache to use.
        ReferenceMap<String,IntegerClauseImpl> weakCache = WEAK_CACHE.get(predicate2evaluatorFactory.getSite());
        if(weakCache == null){

            weakCache = new ReferenceMap<String,IntegerClauseImpl>(
                    ReferenceMap.Type.WEAK,
                    new ConcurrentHashMap<String,Reference<IntegerClauseImpl>>(
                        WEAK_CACHE_INITIAL_CAPACITY,
                        WEAK_CACHE_LOAD_FACTOR,
                        WEAK_CACHE_CONCURRENCY_LEVEL));

            WEAK_CACHE.put(predicate2evaluatorFactory.getSite(), weakCache);
        }

        // use helper method from AbstractLeafClause
        return createClause(
                IntegerClauseImpl.class,
                term,
                field,
                predicate2evaluatorFactory,
                weakCache);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param field the field for this clause. <b>May be <code>null</code></b>.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected IntegerClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, field, knownPredicates, possiblePredicates);
    }

}
