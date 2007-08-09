/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.site.Site;

/**
 *
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class PhraseClauseImpl extends AbstractLeafClause implements PhraseClause {

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;
    
    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,Map<String,WeakReference<PhraseClauseImpl>>> WEAK_CACHE
            = new ConcurrentHashMap<Site,Map<String,WeakReference<PhraseClauseImpl>>>();

    /* A WordClauseImpl specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    static {
        final Collection<TokenPredicate> predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators
        predicates.addAll(TokenPredicate.getTriggerTokenPredicates());

        predicates.add(TokenPredicate.ORGNR);
        predicates.add(TokenPredicate.COMPANYSUFFIX);
        predicates.add(TokenPredicate.SITEPREFIX);

        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    /**
     * Creator method for PhraseClauseImpl objects. By avoiding the constructors,
     * and assuming all PhraseClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the PhraseClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     *
     *
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param engine the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a PhraseClauseImpl instance matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static PhraseClauseImpl createPhraseClause(
            final String term,
            final String field,
            final TokenEvaluationEngine engine) {

        // the weakCache to use.
        Map<String,WeakReference<PhraseClauseImpl>> weakCache = WEAK_CACHE.get(engine.getSite());
        if( weakCache == null ){
            
            weakCache = new ConcurrentHashMap<String,WeakReference<PhraseClauseImpl>>(
                    WEAK_CACHE_INITIAL_CAPACITY, 
                    WEAK_CACHE_LOAD_FACTOR,
                    WEAK_CACHE_CONCURRENCY_LEVEL);
            
            WEAK_CACHE.put(engine.getSite(),weakCache);
        }

        // use helper method from AbstractLeafClause
        return createClause(
                PhraseClauseImpl.class,
                term,
                field,
                engine,
                PREDICATES_APPLICABLE, weakCache);
    }


    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param field the field for this clause. <b>May be <code>null</code></b>.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected PhraseClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, field, knownPredicates, possiblePredicates);

    }
}
