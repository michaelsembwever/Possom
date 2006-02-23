/* Copyright (2005-2006) Schibsted Søk AS
 * RunningQueryContext.java
 *
 * Created on 23 February 2006, 14:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.run;

import no.schibstedsok.common.ioc.BaseContext;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface RunningQueryContext extends BaseContext{
    RunningQuery getRunningQuery();
}
