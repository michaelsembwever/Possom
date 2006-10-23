/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query;

/** Interface for Classes that will implement the Visitor pattern.
 * See complimentary Visitable interface.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Visitor {

    /** Method to hold implementation for what the visitor is supposed to do to the clause object.
     *
     * @param clause the object the visitor will operate on.
     */
    void visit(Object clause);
}
