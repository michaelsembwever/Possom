/* Copyright (2005-2006) Schibsted Søk AS
 * Visitable.java
 *
 * Created on 7 January 2006, 16:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Visitable {
    /**
     *
     * @param visitor
     */
    void accept(Visitor visitor);
}
