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
import no.schibstedsok.front.searchportal.query.EmailClause;
import no.schibstedsok.front.searchportal.query.IntegerClause;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.site.Site;

/**
 * EmailClauseImpl. Contains one email address.
 * 
 * <b>Objects of this class are immutable</b>
 * 
 * @author <a hrefIntegerClauseImpl@wever.org">Michael Semb Wever</a>
 * @version $Id: IntegerClauseImpl.java 2335 2006-02-18 13:45:11Z mickw $
 */
public final class EmailClauseImpl extends AbstractLeafClause implements EmailClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<Site,Map<String,WeakReference<EmailClauseImpl>>> WEAK_CACHE 
            = new HashMap<Site,Map<String,WeakReference<EmailClauseImpl>>>();
    
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
     * Creator method for EmailClauseImpl objects. By avoiding the constructors,
     * and assuming all EmailClauseImpl objects are immutable, we can keep track
     * (via a weak reference map) of instances already in use in this JVM and reuse
     * them.
     * The methods also allow a chunk of creation logic for the EmailClauseImpl to be moved
     * out of the QueryParserImpl.jj file to here.
     * 
     * @param term the term this clause represents.
     * @param field any field this clause was specified against.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a EmailClauseImpl matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static EmailClauseImpl createEmailClause(
        final String term,
        final String field,
        final TokenEvaluationEngine predicate2evaluatorFactory) {

        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);
        
        // the weakCache to use.
        Map<String,WeakReference<EmailClauseImpl>> weakCache = WEAK_CACHE.get(predicate2evaluatorFactory.getSite());
        if( weakCache == null ){
            weakCache = new HashMap<String,WeakReference<EmailClauseImpl>>();
            WEAK_CACHE.put(predicate2evaluatorFactory.getSite(),weakCache);
        }
        
        // use helper method from AbstractLeafClause
        return createClause(
                EmailClauseImpl.class,
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
    protected EmailClauseImpl(
            final String term,
            final String field,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {
        
       super(term, field, knownPredicates, possiblePredicates);
    }

}
