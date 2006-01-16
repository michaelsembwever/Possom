/* Copyright (2005-2006) Schibsted Søk AS
 * LeafClause.java
 *
 * Created on 11 January 2006, 14:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface LeafClause extends Clause {
    String getField();
}
