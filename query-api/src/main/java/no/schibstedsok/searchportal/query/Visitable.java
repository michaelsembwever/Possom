/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 * Visitable.java
 *
 * Created on 7 January 2006, 16:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.query;

import java.io.Serializable;

/** Interface for Classes that will implement the Visitor pattern.
 * See complimentary Visitor interface.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Visitable extends Serializable {

    /** Method to accept a visitor.
     * This method usually doesn't do more than
     * <code>visitor.visit(this);</code>
     *
     * @param visitor the visitor knocking on the door.
     */
    void accept(Visitor visitor);
}
