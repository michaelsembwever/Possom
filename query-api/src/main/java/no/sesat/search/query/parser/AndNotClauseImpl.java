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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.sesat.commons.ref.ReferenceMap;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.token.EvaluationState;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.site.Site;

/**
 * The AndNotClauseImpl represents a joining not clause between two terms in the query.
 * For example: "term1 ANDNOT term2".
 * <b>Objects of this class are immutable</b>
 *
 *
 * @version $Id$
 */
public final class AndNotClauseImpl extends AbstractOperationClause implements AndNotClause {

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    /**
     * Values are WeakReference object to AndNotClauseImpl.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     * An overlap of creation is non-critical.
     */
    private static final Map<Site,ReferenceMap<String,AndNotClauseImpl>> WEAK_CACHE
            = new ConcurrentHashMap<Site,ReferenceMap<String,AndNotClauseImpl>>();

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

        try{
            // create predicate sets
            engine.setState(new EvaluationState(term, new HashSet<TokenPredicate>(), new HashSet<TokenPredicate>()));

            // the weakCache to use.
            ReferenceMap<String,AndNotClauseImpl> weakCache = WEAK_CACHE.get(engine.getSite());
            if( weakCache == null ){

                weakCache = new ReferenceMap<String,AndNotClauseImpl>(
                        DFAULT_REFERENCE_MAP_TYPE,
                        new ConcurrentHashMap<String,Reference<AndNotClauseImpl>>(
                            WEAK_CACHE_INITIAL_CAPACITY,
                            WEAK_CACHE_LOAD_FACTOR,
                            WEAK_CACHE_CONCURRENCY_LEVEL));

                WEAK_CACHE.put(engine.getSite(), weakCache);
            }

            // use helper method from AbstractLeafClause
            return createClause(
                    AndNotClauseImpl.class,
                    term,
                    first,
                    null,
                    engine,
                    weakCache);

        }finally{
            engine.setState(null);
        }
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
