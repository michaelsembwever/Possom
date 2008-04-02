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
import no.sesat.search.query.EmailClause;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.site.Site;

/**
 * EmailClauseImpl. Contains one email address.
 *
 * <b>Objects of this class are immutable</b>
 *
 * @author <a hrefIntegerClauseImpl@wever.org">Michael Semb Wever</a>
 * @version $Id: IntegerClauseImpl.java 2335 2006-02-18 13:45:11Z mickw $
 */
public final class EmailClauseImpl extends AbstractLeafClause implements EmailClause {

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,ReferenceMap<String,EmailClauseImpl>> WEAK_CACHE
            = new ConcurrentHashMap<Site,ReferenceMap<String,EmailClauseImpl>>();

    /**
     * Creator method for EmailClauseImpl objects. By avoiding the constructors,
     * and assuming all EmailClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the EmailClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     *
     *
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param engine the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a EmailClauseImpl matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static EmailClauseImpl createEmailClause(
        final String term,
        final String field,
        final TokenEvaluationEngine engine) {

        // the weakCache to use.
        ReferenceMap<String,EmailClauseImpl> weakCache = WEAK_CACHE.get(engine.getSite());
        if( weakCache == null ){

            weakCache = new ReferenceMap<String,EmailClauseImpl>(
                    ReferenceMap.Type.WEAK,
                    new ConcurrentHashMap<String,Reference<EmailClauseImpl>>(
                        WEAK_CACHE_INITIAL_CAPACITY,
                        WEAK_CACHE_LOAD_FACTOR,
                        WEAK_CACHE_CONCURRENCY_LEVEL));

            WEAK_CACHE.put(engine.getSite(), weakCache);
        }

        // use helper method from AbstractLeafClause
        return createClause(
                EmailClauseImpl.class,
                term,
                field,
                engine,
                weakCache);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param field the field for this clause. <b>May be <code>null</code></b>.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected EmailClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

       super(term, field, knownPredicates, possiblePredicates);
    }

}
