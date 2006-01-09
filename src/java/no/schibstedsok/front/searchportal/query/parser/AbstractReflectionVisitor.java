/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractReflectionVisitor.java
 *
 * Created on 7 January 2006, 16:12
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** A helper implementation of the Visitor pattern using java's reflection.
 * This results in not having to add overloaded methods for each subclass of clause as this implementation will
 * automatically find those overloaded methods without explicitly having to call them in each Clause class.
 * This saves alot of work when adding new Clause subclasses.
 *
 * The overloaded method name is specified by VISIT_METHOD_IMPL.
 *
 * See http://www.javaworld.com/javaworld/javatips/jw-javatip98.html
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractReflectionVisitor implements Visitor {
    /** String specifying name of method used to overload by any class extending this. **/
    public static final String VISIT_METHOD_IMPL = "visitImpl";

    private static final Log LOG = LogFactory.getLog(AbstractReflectionVisitor.class);

    private static final String ERR_CLAUSE_SUBTYPE_NOT_FOUND = "Current visitor implementation does not handle visiting "
            + "non clause subtypes. Tried to visit object: ";
    private static final String ERR_FAILED_TO_VISIT = "Failed to visit object: ";
    private static final String ERR_FAILED_TO_FIND_VISIT_IMPL_OBJECT = "Failed to find method that exists in this class!!";
    private static final String DEBUG_LOOKING_AT = "Looking for method "
            + VISIT_METHOD_IMPL + " with parameter ";


    /** Creates a new instance of AbstractReflectionVisitor.
     */
    public AbstractReflectionVisitor() {
    }

    /**
     * Method implementing Visitor interface. Uses reflection to find the method with name VISIT_METHOD_IMPL with the
     * closest match to the clause subclass.
     * @param clause
     */
    public void visit(final Object clause) {
        final Method method = getMethod(clause.getClass());
        try {
            method.invoke(this, new Object[] {clause});
        } catch (IllegalArgumentException ex) {
            LOG.error(ERR_FAILED_TO_VISIT + clause, ex);
        } catch (InvocationTargetException ex) {
            LOG.error(ERR_FAILED_TO_VISIT + clause, ex);
        } catch (IllegalAccessException ex) {
            LOG.error(ERR_FAILED_TO_VISIT + clause, ex);
        }
    }

    /**
     * You must implement this method at the minimum.
     * Default handling of a visit to any Clause object where a more appropriate overloaded method is not implemented.
     * @param clause
     */
    public abstract void visitImpl(Clause clause);

    /**
     * Final fallback method. This means that the object being visited is not a Clause (or subclass of) object!
     * This behaviour is not intendedly supported and this implementation throws an IllegalArgumentException!
     * @param clause
     */
    public void visitImpl(final Object clause) {
        throw new IllegalArgumentException(ERR_CLAUSE_SUBTYPE_NOT_FOUND + clause.getClass().getName());
    }

    private Method getMethod(final Class clauseClass) {
        final Class me = getClass();
        Method method = null;
        Class currClauseClass = clauseClass;

        LOG.debug("start getMethod");

        // Try the superclasses
        while (method == null && currClauseClass != Object.class) {
            LOG.debug(DEBUG_LOOKING_AT + currClauseClass.getName());
            try {
                method = me.getMethod(VISIT_METHOD_IMPL, new Class[] {currClauseClass});

            } catch (NoSuchMethodException e) {
                currClauseClass = currClauseClass.getSuperclass();
            }
        }

        // Try the interfaces.
        if (method == null) {
            method = getMethodFromInterface(clauseClass);
        }

        // fallback to visitImpl(Object)
        if (method == null) {
            try {
                method = me.getMethod(VISIT_METHOD_IMPL, new Class[] {Object.class});

            } catch (SecurityException ex) {
                LOG.error(ERR_FAILED_TO_FIND_VISIT_IMPL_OBJECT, ex);
            } catch (NoSuchMethodException ex) {
                LOG.fatal(ERR_FAILED_TO_FIND_VISIT_IMPL_OBJECT, ex);
            }

        }
        LOG.debug("Found <" + method.toString() + ">");
        LOG.debug("end getMethod");
        return method;
    }

    /** The interfaces in this array will already be in a suitable order.
        According to java reflection's getMethod contract this order will match the order listed in the
        implements(/extends) definition of the Clause subclass.
     **/
    private Method getMethodFromInterface(final Class clauseClass) {
        final Class me = getClass();
        Method method = null;

        LOG.debug("start getMethodFromInterface");

        final Class[] interfaces = clauseClass.getInterfaces();
        for (int i = 0; i < interfaces.length && method == null; i++) {

            LOG.debug(DEBUG_LOOKING_AT + clauseClass.getName());

            try {
                method = me.getMethod(VISIT_METHOD_IMPL, new Class[] {interfaces[i]});

            } catch (NoSuchMethodException e) {
                // [RECURSION] Look for super interfaces
                method = getMethodFromInterface(interfaces[i]);
                // still null? look at next interface
            }
        }
        LOG.debug("Found <" + method + ">");
        LOG.debug("end getMethodFromInterface");
        return method;
    }

}
