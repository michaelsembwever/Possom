/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractReflectionVisitor.java
 *
 * Created on 7 January 2006, 16:12
 *
 */

package no.schibstedsok.searchportal.query.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import no.schibstedsok.searchportal.query.Visitor;
import org.apache.log4j.Logger;


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

    private static final Logger LOG = Logger.getLogger(AbstractReflectionVisitor.class);

    private static final String ERR_CLAUSE_SUBTYPE_NOT_FOUND = "Current visitor implementation does not handle visiting "
            + "non clause subtypes. Tried to visit object ";
    private static final String ERR_FAILED_TO_VISIT = "Failed to visit object ";
    private static final String ERR_FAILED_TO_FIND_VISIT_IMPL_OBJECT = "Failed to find method that exists in this class!!"
            + "Was trying to visit object ";
    private static final String DEBUG_LOOKING_AT = "Looking for method "
            + VISIT_METHOD_IMPL + "(";
    private static final String TRACE_KEEP_LOOKING = "keep looking";
    private static final String RB = ")";



    /** Creates a new instance of AbstractReflectionVisitor.
     */
    public AbstractReflectionVisitor() {
    }

    /**
     * Method implementing Visitor interface. Uses reflection to find the method with name VISIT_METHOD_IMPL with the
     * closest match to the clause subclass.
     * @param clause the clause we're visiting.
     */
    public void visit(final Object clause) {
        final Method method = getMethod(clause.getClass());
        try {
            method.setAccessible(true);
            method.invoke(this, new Object[] {clause});

        } catch (IllegalArgumentException ex) {
            LOG.error(ERR_FAILED_TO_VISIT + clause, ex);
        } catch (InvocationTargetException ex) {
            LOG.error(ERR_FAILED_TO_VISIT + clause, ex);
        } catch (IllegalAccessException ex) {
            LOG.error(ERR_FAILED_TO_VISIT + clause, ex);
        }  finally  {
            method.setAccessible(false);
        }
    }

    /**
     * Final fallback method. This means that the object being visited is not a Clause (or subclass of) object!
     * This behaviour is not intendedly supported and this implementation throws an IllegalArgumentException!
     * @param clause the clause we're visiting (that's not acutally a clause subtype ;)
     */
    protected void visitImpl(final Object clause) {
        throw new IllegalArgumentException(ERR_CLAUSE_SUBTYPE_NOT_FOUND + clause.getClass().getName());
    }

    private Method getMethod(final Class clauseClass) {

        // XXX This is one of the applications performance hotspots.
        //  It could be benefit to keep a weak reference map to remember what method to use.

        Method method = null;

        LOG.trace("getMethod(" + clauseClass.getName() + ")");

        // Try the superclasses
        Class currClauseClass = clauseClass;
        while (method == null && currClauseClass != Object.class) {
            LOG.trace(DEBUG_LOOKING_AT + currClauseClass.getName() + RB);

                method = getDeclaredMethod(currClauseClass);

            if (method == null) {
                currClauseClass = currClauseClass.getSuperclass();
            }
        }

        // Try the interfaces.
        // Gets alittle bit tricky because we must not only search subinterfaces
        //  but search both interfaces and superinterfaces of superclasses...
        currClauseClass = clauseClass;
        while (method == null && currClauseClass != Object.class) {

            method = getMethodFromInterface(currClauseClass);
            currClauseClass = currClauseClass.getSuperclass();
        }

        // fallback to visitImpl(Object)
        if (method == null) {

            method = getDeclaredMethod(Object.class);

            if (method == null) {
                LOG.fatal(ERR_FAILED_TO_FIND_VISIT_IMPL_OBJECT + clauseClass.getName());
            }

        }
        LOG.trace("end getMethod(" + clauseClass.getName() + ")");
        return method;
    }

    /** The interfaces in this array will already be in a suitable order.
        According to java reflection's getMethod contract this order will match the order listed in the
        implements(/extends) definition of the Clause subclass.
     **/
    private Method getMethodFromInterface(final Class clauseClass) {

        Method method = null;

        LOG.trace("getMethodFromInterface(" + clauseClass.getName() + ")");

        final Class[] interfaces = clauseClass.getInterfaces();
        for (int i = 0; i < interfaces.length && method == null; i++) {

            LOG.trace(DEBUG_LOOKING_AT + interfaces[i].getName() + RB);


            method = getDeclaredMethod(interfaces[i]);


            if (method == null) {
                // [RECURSION] Look for super interfaces
                method = getMethodFromInterface(interfaces[i]);
            }  else  {
                // This is the most useful log statement in this file, 
                //  but gets called too many times per request to be promoted to debug.
                LOG.trace("Found method accepting <" + interfaces[i].getSimpleName()
                        + "> in " + method.getDeclaringClass().getSimpleName());
            }
        }

        LOG.trace("end getMethodFromInterface(" + clauseClass.getName() + ")");
        return method;
    }

    /** Because Class.getDeclaredMethod(..) behaves differently to getMethod(..) in
     * that it does not look into superclasses we must manually look through the superclass
     * heirarchy. We don't want to use getMethod(..) either because it will only return public methods,
     * and we would like our visitImpl methods to remain private/protected.
     **/
    private Method getDeclaredMethod(final Class clauseClass) {

        for (Class cls = getClass();; cls = cls.getSuperclass()) {
            if (cls != null) {
                try {
                    return cls.getDeclaredMethod(VISIT_METHOD_IMPL, new Class[] {clauseClass});

                } catch (NoSuchMethodException e) {
                    LOG.trace(TRACE_KEEP_LOOKING);
                }
            }  else  {
                return null;
            }
        }
    }

}
