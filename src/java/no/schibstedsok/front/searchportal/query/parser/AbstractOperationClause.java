/* Copyright (2005-2006) Schibsted Søk AS
 * AbstractOperationClause.java
 *
 * Created on 7 January 2006, 16:05
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractOperationClause extends AbstractClause implements OperationClause {
    
    private static final Log LOG = LogFactory.getLog(AbstractLeafClause.class);
    
    /** Works off the assumption that OperationClause constructor's have the exact parameter list:
     *       final String term,
     *       final Clause left,
     *       final Clause right,
     *       final Set<Predicate> knownPredicates,
     *       final Set<Predicate> possiblePredicates
     *
     * Where this is true subclasses are free to use this helper method.
     **/
    public static AbstractOperationClause createClause(
            final Class/*<? extends OperationClause>*/ clauseClass,
            final String term,
            final Clause left,
            final Clause right,
            final TokenEvaluatorFactory predicate2evaluatorFactory,
            final Collection/*<Predicate>*/ predicates2check,
            final Map/*<Long,WeakReference<? extends AbstractOperationClause>>*/ weakCache ) {


        final String key = term; // important that the key argument is unique to this object.

        // check weak reference cache of immutable wordClauses here.
        // no need to synchronise, no big lost if duplicate identical objects are created and added over each other
        //  into the cache, compared to the performance lost of trying to synchronise this.
        AbstractOperationClause clause = (AbstractOperationClause) findClauseInUse(key, weakCache);

        if ( clause == null ) {
            // Doesn't exist in weak-reference cache. let's find the predicates and create the WordClause.
            final Set/*<Predicate>*/ knownPredicates  = new TreeSet/*<Predicate>*/();
            final Set/*<Predicate>*/ possiblePredicates  = new TreeSet/*<Predicate>*/();

            // find the applicale predicates now
            findPredicates(predicate2evaluatorFactory, predicates2check, knownPredicates, possiblePredicates);
            try {
                // find the constructor...
                final Constructor constructor = clauseClass.getDeclaredConstructor(new Class[]{
                    String.class, Clause.class, Clause.class, Set.class, Set.class
                });
                // use the constructor...
                clause = (AbstractOperationClause) constructor.newInstance(new Object[]{
                    term, left, right, knownPredicates, possiblePredicates
                });

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

    protected AbstractOperationClause(){}
    
    protected AbstractOperationClause(
            final String term,
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates){
        
        super(term,knownPredicates,possiblePredicates);
    }
}
