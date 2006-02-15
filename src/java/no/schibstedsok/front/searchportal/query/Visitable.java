/* Copyright (2005-2006) Schibsted Søk AS
 * Visitable.java
 *
 * Created on 7 January 2006, 16:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query;

import no.schibstedsok.front.searchportal.query.parser.*;

/** Interface for Classes that will implement the Visitor pattern.
 * See complimentary Visitor interface.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Visitable {

    /** Method to accept a visitor.
     * This method usually doesn't do more than
     * <code>visitor.visit(this);</code>
     *
     * @param visitor the visitor knocking on the door.
     */
    void accept(Visitor visitor);
}
