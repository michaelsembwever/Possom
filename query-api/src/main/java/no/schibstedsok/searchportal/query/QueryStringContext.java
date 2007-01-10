/* Copyright (2005-2006) Schibsted Søk AS
 * QueryStringContext.java
 *
 * Created on 23 January 2006, 14:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.query;

import no.schibstedsok.commons.ioc.BaseContext;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface QueryStringContext extends BaseContext{
    /** Get the original query string.
     *
     * @return the original query string.
     */
    String getQueryString();
}
