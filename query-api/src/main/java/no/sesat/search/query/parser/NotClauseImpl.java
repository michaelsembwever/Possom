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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.token.EvaluationState;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.site.Site;

/**
 * The NotClauseImpl represents a not clause between prefixing another term in the query.
 * For example: "NOT term1".
 * <b>Objects of this class are immutable</b>
 *
 * @author <a hrefNotClauseImplmick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class NotClauseImpl extends AbstractOperationClause implements NotClause {

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;
    
    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,Map<String,WeakReference<NotClauseImpl>>> WEAK_CACHE
            = new ConcurrentHashMap<Site,Map<String,WeakReference<NotClauseImpl>>>();

    /* A WordClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    static {
        final Collection<TokenPredicate> predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);

        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    /**
     * Creator method for NotClauseImpl objects. By avoiding the constructors,
     * and assuming all NotClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the NotClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     *
     * @param first the left child clause of the operation clause we are about to create (or find).
     * @param engine the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a NoNotClauseImplnstance matching the term, and left child clauses.
     * May be either newly created or reused.
     */
    public static NotClauseImpl createNotClause(
        final Clause first,
        final TokenEvaluationEngine engine) {

        // construct the proper "schibstedsøk" formatted term for this operation.
        //  XXX eventually it would be nice not to have to expose the internal string representation of this object.
        final String term = "NOT "
                + (first instanceof LeafClause && ((LeafClause) first).getField() != null
                    ?  ((LeafClause) first).getField() + ":"
                    : "")
                + first.getTerm();

        try{
            // create predicate sets
            engine.setState(new EvaluationState(term, new HashSet<TokenPredicate>(), new HashSet<TokenPredicate>()));

            // the weakCache to use.
            Map<String,WeakReference<NotClauseImpl>> weakCache = WEAK_CACHE.get(engine.getSite());
            if( weakCache == null ){
                
                weakCache = new ConcurrentHashMap<String,WeakReference<NotClauseImpl>>(
                        WEAK_CACHE_INITIAL_CAPACITY,
                        WEAK_CACHE_LOAD_FACTOR,
                        WEAK_CACHE_CONCURRENCY_LEVEL);
                
                WEAK_CACHE.put(engine.getSite(),weakCache);
            }

            // use helper method from AbstractLeafClause
            return createClause(
                    NotClauseImpl.class,
                    term,
                    first,
                    null,
                    engine,
                    PREDICATES_APPLICABLE, weakCache);

        }finally{
            engine.setState(null);
        }
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
            final Clause first,
            final Clause second,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, first, knownPredicates, possiblePredicates);
    }

}
