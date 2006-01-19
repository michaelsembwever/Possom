/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractClause.java
 *
 * Created on 11 January 2006, 14:17
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluator;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenPredicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** Basic implementation of the Clause interface.
 * Provides basic implementation of the predicates lists, defaulting to empty lists.
 * Also provides weak reference hashmaps to keep record and reuse the Clauses already in use in the JVM.
 * <b>Objects of this class are immutable</b>
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractClause implements Clause {

    private static final Log LOG = LogFactory.getLog(AbstractClause.class);
    private static final String DEBUG_REFERENCE_REUSED = "Gjenbruk weakReference. Size nå ";

    private final String term;
    private final Set/*<Predicate>*/ knownPredicates;
    private final Set/*<Predicate>*/ possiblePredicates;





    /**
     * See if there is an identical and immutable Clause already in use in the JVM.
     * @param key the <B>unique</B> (for this AbstractClause subtype) key for the Clause we are looking for.
     * @param weakCache the map containing the key to WeakReference (of the Clause) mappings.
     * @return the AbstractClause in use already, matching the key. <B>May be <CODE>null</CODE></B>.
     */
    protected static final AbstractClause findClauseInUse(
            final String key,
            final Map/*<String,WeakReference<? extends AbstractClause>>*/ weakCache) {

        AbstractClause result = null;

        final WeakReference/*<AbstractClause>*/ weakRef = (WeakReference) weakCache.get(key);
        if (weakRef != null) {
            result = (AbstractClause) weakRef.get();
        }
        if (result != null && LOG.isDebugEnabled()) {
            LOG.debug(DEBUG_REFERENCE_REUSED + weakCache.size());
        }

        return result;
    }

    /**
     * Note there is an identical and immutable Clause ready to use in the JVM.
     * @param key the <B>unique</B> (for this AbstractClause subtype) key for the Clause we are about to add to the mappings.
     * @param clause the Clause we are about to add to the mappings.
     * @param weakCache the map containing the key to WeakReference (of the Clause) mappings.
     */
    protected static final void addClauseInUse(
            final String key,
            final AbstractClause clause,
            final Map/*<Long,WeakReference<? extends AbstractClause>>*/ weakCache) {

        weakCache.put(key, new WeakReference/*<AbstractClause>*/(clause) {
            public void clear() {
                // clear the hashmap entry too!
                weakCache.remove(key);
                // clear the referent
                super.clear();
            }
        });
    }

    /**
     * Find the predicates that are applicable to the clause.
     * (Only the clause's term is known and is kept in state inside the TokenEvaluatorFactory).
     * Add known predicates to <CODE>knownPredicates</CODE>.
     * Add possible (requires further checking against the whole query heirarchy) predicates to <CODE>possiblePredicates</CODE>.
     * @param predicate2evaluatorFactory the factory handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     * @param predicates2check the complete list of predicates that could apply to the current clause we are finding predicates for.
     * @param knownPredicates the set to fill out with known predicates.
     * @param possiblePredicates the set to fill out with possible predicates.
     */
    protected static final void findPredicates(
            final TokenEvaluatorFactory predicate2evaluatorFactory,
            final Collection/*<Predicate>*/ predicates2check,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {


        for (Iterator it = predicates2check.iterator(); it.hasNext();) {
            final TokenPredicate token = (TokenPredicate) it.next();
            final TokenEvaluator evaluator = predicate2evaluatorFactory.getEvaluator(token);
            final String currTerm = predicate2evaluatorFactory.getCurrentTerm();

            if (token.evaluate(predicate2evaluatorFactory)) {
                if (evaluator.isQueryDependant()) {
                    possiblePredicates.add(token);
                    LOG.debug(DEBUG_FOUND_PREDICATE_PREFIX + currTerm + DEBUG_FOUND_PREDICATE_POSSIBLE + token);
                }  else  {
                    knownPredicates.add(token);
                    LOG.debug(DEBUG_FOUND_PREDICATE_PREFIX + currTerm + DEBUG_FOUND_PREDICATE_KNOWN + token);
                }
            }
        }
    }

    /** You must use <CODE>AbstractClause(String, Set&lt;Predicate&gt;, Set&lt;Predicate&gt;)</CODE> instead.
     * This constructor will throw an IllegalArgumentException.
     **/
    protected AbstractClause() {
        throw new IllegalArgumentException(ERR_MUST_ALWAYS_USE_ARGED_CONSTRUCTOR);
    }

    /**
     * Create clause with the given term, known and possible predicates.
     * @param term the term (query string) for this clause.
     * @param knownPredicates the set of known predicates for this clause.
     * @param possiblePredicates the set of possible predicates for this clause.
     */
    protected AbstractClause(
            final String term,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {

        this.term = term;
        this.knownPredicates = Collections.unmodifiableSet(knownPredicates);
        this.possiblePredicates = Collections.unmodifiableSet(possiblePredicates);
    }

    /**
     * Get the term for this Clause.
     * Does not include any field values (eg "firstname:").
     * @return the term for this clause.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Get the set of knownPredicates for this Clause.
     * The set is unmodifiable.
     * @return set of knownPredicates.
     */
    public Set/*<Predicate>*/ getKnownPredicates() {
        return knownPredicates;
    }

    /**
     * Get the set of possiblePredicates for this Clause.
     * The set is unmodifiable.
     * @return set of possiblePredicates.
     */
    public Set/*<Predicate>*/ getPossiblePredicates() {
        return possiblePredicates;
    }


    /** {@inheritDoc}
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * Error message when reflection cannot find the required constructor.
     */
    protected static final String ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR
            = "Failed to find (or use) constructor with parameters (String, String, Set, Set) for class: ";
    /**
     * Error message when trying to use the incorrect constructor.
     **/
    protected static final String ERR_MUST_ALWAYS_USE_ARGED_CONSTRUCTOR = "Illegal to call constructor without arguments!";
    private static final String DEBUG_FOUND_PREDICATE_PREFIX = "Found (for \"";
    private static final String DEBUG_FOUND_PREDICATE_KNOWN = "\") known ";
    private static final String DEBUG_FOUND_PREDICATE_POSSIBLE = "\") possible ";
}
