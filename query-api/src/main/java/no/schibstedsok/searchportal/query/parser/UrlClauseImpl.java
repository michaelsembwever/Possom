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
import no.schibstedsok.searchportal.query.IntegerClause;
import no.schibstedsok.searchportal.query.UrlClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.site.Site;

/**
 * UrlClauseImpl. Contains one http URL.
 * 
 * <b>Objects of this class are immutable</b>
 * 
 * @author <a hrefIntegerClauseImpl@wever.org">Michael Semb Wever</a>
 * @version $Id: IntegerClauseImpl.java 2335 2006-02-18 13:45:11Z mickw $
 */
public final class UrlClauseImpl extends AbstractLeafClause implements UrlClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,Map<String,WeakReference<UrlClauseImpl>>> WEAK_CACHE 
            = new HashMap<Site,Map<String,WeakReference<UrlClauseImpl>>>();
    
    /* A IntegerClauseImpl specific collection of TokenPredicates that *could* apply to this Clause type. */
    private static final Collection<TokenPredicate> PREDICATES_APPLICABLE;

    static {
        final Collection<TokenPredicate> predicates = new ArrayList();
        predicates.add(TokenPredicate.ALWAYSTRUE);
        // Predicates from RegExpEvaluators

        // Add all FastTokenPredicates
        predicates.addAll(TokenPredicate.getFastTokenPredicates());
        PREDICATES_APPLICABLE = Collections.unmodifiableCollection(predicates);
    }

    /**
     * Creator method for UrlClauseImpl objects. By avoiding the constructors,
     * and assuming all UrlClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the UrlClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     * 
     * 
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param engine the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a UrlClauseImpl matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static UrlClauseImpl createUrlClause(
        final String term,
        final String field,
        final TokenEvaluationEngine engine) {

        // the weakCache to use.
        Map<String,WeakReference<UrlClauseImpl>> weakCache = WEAK_CACHE.get(engine.getSite());
        if( weakCache == null ){
            weakCache = new HashMap<String,WeakReference<UrlClauseImpl>>();
            WEAK_CACHE.put(engine.getSite(),weakCache);
        }
        
        // use helper method from AbstractLeafClause
        return createClause(
                UrlClauseImpl.class,
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
    protected UrlClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, field, knownPredicates, possiblePredicates);
    }

}
