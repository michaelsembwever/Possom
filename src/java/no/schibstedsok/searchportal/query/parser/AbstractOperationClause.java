/* Copyright (2005-2006) Schibsted Søk AS
 * AbstractOperationClause.java
 *
 * Created on 7 January 2006, 16:05
 *
 */

package no.schibstedsok.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import org.apache.log4j.Logger;

/**
 * Basic implementation of the OperationClause interface.
 * Implements ontop of AbstractClause.
 * <b>Objects of this class are immutable</b>
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractOperationClause extends AbstractClause implements OperationClause {

    private static final Logger LOG = Logger.getLogger(AbstractLeafClause.class);

    /**
     * Works off the assumption that OperationClause constructor's have the exact parameter list:
     *       final String term,
     *       final Clause left,
     *       final Clause right,
     *       final Set&lt;Predicate&gt; knownPredicates,
     *       final Set&lt;Predicate&gt; possiblePredicates
     *
     * Where this is true subclasses are free to use this helper method.
     * @param left the left child clause for OperationClause the clause we are about to create (or find) will have.
     * @param right the right child clause the clause we are about to create (or find) will have.
     * @param clauseClass the exact subclass of AbstracLeafClause that we are about to create (or find already in use).
     * @param term the term the clause we are about to create (or find) will have.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @param predicates2check the complete list of predicates that could apply to the current clause we are finding predicates for.
     * @param weakCache the map containing the key to WeakReference (of the Clause) mappings.
     * @return Either a clause already in use that matches this term and field, or a newly created cluase for this term and field.
     */
    public static <T extends AbstractOperationClause> T createClause(
            final Class<T> clauseClass,
            final String term,
            final Clause left,
            final Clause right,
            final TokenEvaluationEngine predicate2evaluatorFactory,
            final Collection<TokenPredicate> predicates2check,
            final Map<String,WeakReference<T>> weakCache) {


        final String key = term; // important that the key argument is unique to this object.

        // check weak reference cache of immutable wordClauses here.
        // no need to synchronise, no big lost if duplicate identical objects are created and added over each other
        //  into the cache, compared to the performance lost of trying to synchronise this.
        T clause = findClauseInUse(key, weakCache);

        if (clause == null) {
            // Doesn't exist in weak-reference cache. let's find the predicates and create the WordClause.

            // create predicate sets
            predicate2evaluatorFactory.setClausesKnownPredicates(new HashSet<TokenPredicate>());
            predicate2evaluatorFactory.setClausesPossiblePredicates(new HashSet<TokenPredicate>());
            // find the applicale predicates now
            findPredicates(predicate2evaluatorFactory, predicates2check);
            try {
                // find the constructor...
                final Constructor<T> constructor = clauseClass.getDeclaredConstructor(
                    String.class, Clause.class, Clause.class, Set.class, Set.class
                );
                // use the constructor...
                clause = constructor.newInstance(
                    term,
                    left,
                    right,
                    predicate2evaluatorFactory.getClausesKnownPredicates(),
                    predicate2evaluatorFactory.getClausesPossiblePredicates()
                );

            } catch (SecurityException ex) {
                LOG.error(ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR + clauseClass.getName(), ex);
            } catch (NoSuchMethodException ex) {
                LOG.error(ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR + clauseClass.getName(), ex);
            } catch (IllegalArgumentException ex) {
                LOG.error(ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR + clauseClass.getName(), ex);
            } catch (InstantiationException ex) {
                LOG.error(ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR + clauseClass.getName(), ex);
            } catch (InvocationTargetException ex) {
                LOG.error(ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR + clauseClass.getName(), ex);
            } catch (IllegalAccessException ex) {
                LOG.error(ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR + clauseClass.getName(), ex);
            }

            addClauseInUse(key, clause, weakCache);
        }

        return clause;
    }

    /** You must use <CODE>AbstractOperationClause(String, Set&lt;Predicate&gt;, Set&lt;Predicate&gt;)</CODE> instead.
     * This constructor will throw an IllegalArgumentException.
     **/
    protected AbstractOperationClause() {
        throw new IllegalArgumentException(ERR_MUST_ALWAYS_USE_ARGED_CONSTRUCTOR);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected AbstractOperationClause(
            final String term,
            final Clause first,
            final Set<TokenPredicate> knownPredicates,
            final Set<TokenPredicate> possiblePredicates) {

        super(term, knownPredicates, possiblePredicates);
        firstClause = first;
    }



    /** TODO comment me. **/
    protected final Clause firstClause;


    /**
     * Get the firstClause.
     *
     * @return the firstClause.
     */
    public Clause getFirstClause() {
        return firstClause;
    }
}
