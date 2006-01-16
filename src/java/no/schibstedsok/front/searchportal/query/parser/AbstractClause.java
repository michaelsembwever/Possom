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
import java.util.HashSet;
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
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractClause implements Clause {

    private static final Log LOG = LogFactory.getLog(AbstractClause.class);
    private static final String DEBUG_REFERENCE_REUSED = "Gjenbruk weakReference. Size nå ";

    protected final String term;
    private final Set/*<Predicate>*/ knownPredicates = new HashSet/*<Predicate>*/();
    private final Set/*<Predicate>*/ possiblePredicates = new HashSet/*<Predicate>*/();

    

    /** See if there is an identical and immutable Clause already in use in the JVM. **/
    protected static final AbstractClause findClauseInUse(
            final String key,
            final Map/*<Long,WeakReference<? extends AbstractClause>>*/ weakCache) {

        AbstractClause result = null;

        final WeakReference/*<AbstractClause>*/ weakRef = (WeakReference) weakCache.get(key);
        if ( weakRef != null ) {
            result = (AbstractClause) weakRef.get();
        }
        if ( result != null && LOG.isDebugEnabled() ) {
            LOG.debug(DEBUG_REFERENCE_REUSED + weakCache.size());
        }

        return result;
    }

    /** Note there is an identical and immutable Clause ready to use in the JVM. **/
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

    protected static final void findPredicates(
            final TokenEvaluatorFactory predicate2evaluatorFactory,
            final Collection/*<Predicate>*/ predicates2check,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {


        for ( Iterator it = predicates2check.iterator(); it.hasNext(); ) {
            final TokenPredicate token = (TokenPredicate) it.next();
            final TokenEvaluator evaluator = predicate2evaluatorFactory.getEvaluator(token);
            final String currTerm = predicate2evaluatorFactory.getCurrentTerm();

            if ( token.evaluate(predicate2evaluatorFactory) ) {
                if ( evaluator.isQueryDependant() ) {
                    possiblePredicates.add(token);
                    LOG.debug(DEBUG_FOUND_PREDICATE_PREFIX + currTerm + DEBUG_FOUND_PREDICATE_POSSIBLE + token);
                }  else  {
                    knownPredicates.add( token);
                    LOG.debug(DEBUG_FOUND_PREDICATE_PREFIX + currTerm + DEBUG_FOUND_PREDICATE_KNOWN + token);
                }
            }
        }
    }

    /** keep predicate lists as empty **/
    protected AbstractClause() {  term = null; }

    /** update predicate lists **/
    protected AbstractClause(
            final String term,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {

        this.term = term;
        this.knownPredicates.addAll(knownPredicates);
        this.possiblePredicates.addAll(possiblePredicates);
    }

    public String getTerm(){
        return term;
    }

    public Set getKnownPredicates() {
        return Collections.unmodifiableSet(knownPredicates);
    }

    public Set getPossiblePredicates() {
        return Collections.unmodifiableSet(possiblePredicates);
    }


    protected static final String ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR
            = "Failed to find (or use) constructor with parameters (String, String, Set, Set) for class: ";
            
    private static final String DEBUG_FOUND_PREDICATE_PREFIX = "Found (for \"";
    private static final String DEBUG_FOUND_PREDICATE_KNOWN = "\") known ";
    private static final String DEBUG_FOUND_PREDICATE_POSSIBLE = "\") possible ";
}
