/* Copyright (2005-2006) Schibsted Søk AS
 * AbstractLeafClause.java
 *
 * Created on 7 January 2006, 16:06
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
public abstract class AbstractLeafClause extends AbstractClause implements LeafClause {

    private static final Log LOG = LogFactory.getLog(AbstractLeafClause.class);

    private static final String ERR_FAILED_FINDING_OR_USING_CONSTRUCTOR
            = "Failed to find (or use) constructor with parameters (String, String, Set, Set) for class: ";

    /** Works off the assumption that LeafClause constructor's have the exact parameter list:
     *       final String term,
     *       final String field,
     *       final Set<Predicate> knownPredicates,
     *       final Set<Predicate> possiblePredicates
     *
     * Where this is true subclasses are free to use this helper method.
     **/
    public static AbstractLeafClause createClause(
            final Class/*<? extends LeafClause>*/ clauseClass,
            final String term,
            final String field,
            final TokenEvaluatorFactory predicate2evaluatorFactory,
            final Collection/*<Predicate>*/ predicates2check,
            final Map/*<Long,WeakReference<AbstractClause>>*/ weakCache ) {


        final String key = field + ":" + term; // important that the key argument is unique to this object.

        // check weak reference cache of immutable wordClauses here.
        // no need to synchronise, no big lost if duplicate identical objects are created and added over each other
        //  into the cache, compared to the performance lost of trying to synchronise this.
        AbstractLeafClause clause = (AbstractLeafClause) findClauseInUse(key, weakCache);

        if ( clause == null ) {
            // Doesn't exist in weak-reference cache. let's find the predicates and create the WordClause.
            final Set/*<Predicate>*/ knownPredicates  = new TreeSet/*<Predicate>*/();
            final Set/*<Predicate>*/ possiblePredicates  = new TreeSet/*<Predicate>*/();

            // find the applicale predicates now
            findPredicates(predicate2evaluatorFactory, predicates2check, knownPredicates, possiblePredicates);
            try {
                // find the constructor...
                final Constructor constructor = clauseClass.getDeclaredConstructor(new Class[]{
                    String.class, String.class, Set.class, Set.class
                });
                // use the constructor...
                clause = (AbstractLeafClause) constructor.newInstance(new Object[]{
                    term, field, knownPredicates, possiblePredicates
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

    protected AbstractLeafClause() {  }

    protected AbstractLeafClause(
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates) {

        super(knownPredicates, possiblePredicates);
    }
}
