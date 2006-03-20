/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import org.apache.commons.collections.Predicate;

/**
 * The XorClauseImpl represents a joining clause between two terms in the query.
 * For example: "term1 OR term2".
 * <b>Objects of this class are immutable</b>
 * 
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: OrClauseImpl.java 2344 2006-02-20 20:07:12Z mickw $
 */
public final class XorClauseImpl extends OrClauseImpl implements XorClause {

    /** Values are WeakReference object to AbstractClause.
     * Unsynchronized are there are no 'changing values', just existance or not of the AbstractClause in the system.
     */
    private static final Map<String,WeakReference<XorClauseImpl>> WEAK_CACHE = new HashMap<String,WeakReference<XorClauseImpl>>();

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
     * @param first the left child clause of the operation clause we are about to create (or find).
     * @param second the right child clause of the operation clause we are about to create (or find).
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @return returns a XorClauseImpl matching the term, left and right child clauses.
     * May be either newly created or reused.
     */
    public static XorClauseImpl createXorClause(
        final Clause first,
        final Clause second,
        final Hint hint,
        final TokenEvaluatorFactory predicate2evaluatorFactory) {

        // construct the proper "schibstedsøk" formatted term for this operation.
        //  XXX eventually it would be nice not to have to expose the internal string representation of this object.
        final String term = 
                (first instanceof LeafClause && ((LeafClause) first).getField() != null
                    ?  ((LeafClause) first).getField() + ":"
                    : "")
                + first.getTerm()
                + " "
                + (second instanceof LeafClause && ((LeafClause) second).getField() != null
                    ?  ((LeafClause) second).getField() + ":"
                    : "")
                + second.getTerm();

        // update the factory with what the current term is
        predicate2evaluatorFactory.setCurrentTerm(term);

        // we can't use the helper method because of the extra Hint argument to the XorClauseImpl constructor        

        // check weak reference cache of immutable wordClauses here.
        // no need to synchronise, no big lost if duplicate identical objects are created and added over each other
        //  into the cache, compared to the performance lost of trying to synchronise this.
        XorClauseImpl clause = findClauseInUse(term, WEAK_CACHE);

        if (clause == null) {
            // Doesn't exist in weak-reference cache. let's find the predicates and create the WordClause.
            
            // create predicate sets
            predicate2evaluatorFactory.setClausesKnownPredicates(new HashSet<TokenPredicate>());
            predicate2evaluatorFactory.setClausesPossiblePredicates(new HashSet<TokenPredicate>());
            // find the applicale predicates now
            findPredicates(predicate2evaluatorFactory, PREDICATES_APPLICABLE);

            // create it...
            clause = new XorClauseImpl(
                term, 
                first, 
                second, 
                hint,
                predicate2evaluatorFactory.getClausesKnownPredicates(), 
                predicate2evaluatorFactory.getClausesPossiblePredicates()
            );

            addClauseInUse(term, clause, WEAK_CACHE);
        }

        return clause;        
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
