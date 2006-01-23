/* Copyright (2005-2006) Schibsted Søk AS
 * QueryStringContext.java
 *
 * Created on 23 January 2006, 14:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryStringContext {
    /** Get the original query string.
     *
     * @return the original query string.
     */
    String getQueryString();
}
