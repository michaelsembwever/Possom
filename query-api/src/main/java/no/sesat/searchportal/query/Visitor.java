/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal.query;

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
