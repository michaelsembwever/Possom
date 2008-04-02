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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.sesat.commons.ref.ReferenceMap;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.token.EvaluationState;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.site.Site;

/**
 * The XorClauseImpl represents a joining clause between two terms in the query.
 * For example: "term1 OR term2".
 * <b>Objects of this class are immutable</b>
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: OrClauseImpl.java 2344 2006-02-20 20:07:12Z mickw $
 */
public final class XorClauseImpl extends OrClauseImpl implements XorClause {

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,ReferenceMap<String,XorClauseImpl>> WEAK_CACHE
            = new ConcurrentHashMap<Site,ReferenceMap<String,XorClauseImpl>>();

    /* A WordClause specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    private final Hint hint;

    static {
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(new ArrayList());
    }

    /**
     * Creator method for XorClauseImpl objects. By avoiding the constructors,
     * and assuming all XorClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the XorClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     *
     *
     * @param first the left child clause of the operation clause we are about to create (or find).
     * @param second the right child clause of the operation clause we are about to create (or find).
     * @param hint
     * @param engine the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a XorClauseImpl matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static XorClauseImpl createXorClause(
        final Clause first,
        final Clause second,
        final Hint hint,
        final TokenEvaluationEngine engine) {

        // construct the proper "schibstedsøk" formatted term for this operation.
        //  XXX eventually it would be nice not to have to expose the internal string representation of this object.
        final String term =
                (first instanceof LeafClause && ((LeafClause) first).getField() != null
                    ?  ((LeafClause) first).getField() + ':'
                    : "")
                + first.getTerm()
                + " "
                + (second instanceof LeafClause && ((LeafClause) second).getField() != null
                    ?  ((LeafClause) second).getField() + ':'
                    : "")
                + second.getTerm();
        try{
            // create predicate sets
            engine.setState(new EvaluationState(term, new HashSet<TokenPredicate>(), new HashSet<TokenPredicate>()));

            // the weakCache to use.
            ReferenceMap<String,XorClauseImpl> weakCache = WEAK_CACHE.get(engine.getSite());
            if( weakCache == null ){

                weakCache = new ReferenceMap<String,XorClauseImpl>(
                        ReferenceMap.Type.WEAK,
                        new ConcurrentHashMap<String,Reference<XorClauseImpl>>(
                        WEAK_CACHE_INITIAL_CAPACITY,
                        WEAK_CACHE_LOAD_FACTOR,
                        WEAK_CACHE_CONCURRENCY_LEVEL));

                WEAK_CACHE.put(engine.getSite(), weakCache);
            }

            // we can't use the helper method because of the extra Hint argument to the XorClauseImpl constructor

            // check weak reference cache of immutable wordClauses here.
            // no need to synchronise, no big lost if duplicate identical objects are created and added over each other
            //  into the cache, compared to the performance lost of trying to synchronise this.
            XorClauseImpl clause = findClauseInUse(term, weakCache);

            if (clause == null) {
                // Doesn't exist in weak-reference cache. let's find the predicates and create the WordClause.

                // find the applicale predicates now
                final boolean healthy = findPredicates(engine);

                // create it...
                clause = new XorClauseImpl(
                    term,
                    first,
                    second,
                    hint,
                    engine.getState().getKnownPredicates(),
                    engine.getState().getPossiblePredicates()
                );

                if( healthy ){
                    addClauseInUse(term, clause, weakCache);
                }
            }

            return clause;

        }finally{
            engine.setState(null);
        }
    }

    public XorClause.Hint getHint() {
        return hint;
    }

    /**
     * Create the XorClauseImpl with the given term, left and right child clauses, and known and possible predicate sets.
     *
     * @param term the term for this OrClauseImpl.
     * @param knownPredicates set of known predicates.
     * @param possiblePredicates set of possible predicates.
     * @param first the left child clause.
     * @param second the right child clause.
     */
    protected XorClauseImpl(
            final String term,
            final Clause first,
            final Clause second,
            final Hint hint,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, first, second, knownPredicates, possiblePredicates);
        this.hint = hint;
    }
}
