/* Copyright (2005-2006) Schibsted Søk AS
 * QueryVisitor.java
 *
 * Created on 7 January 2006, 20:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.parser;

/** Satisfies the contract that a visitor to a Query must supply once it has done the visiting.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryVisitor extends Visitor  {
    /**
     *
     * @return
     */
    String getQueryAsString();
}
