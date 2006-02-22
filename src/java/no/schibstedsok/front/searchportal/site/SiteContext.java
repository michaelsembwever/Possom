/* Copyright (2005-2006) Schibsted Søk AS
 * SiteContext.java
 *
 * Created on 23 January 2006, 13:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.site;

import no.schibstedsok.common.ioc.BaseContext;

/** Defines the context for consumers of Site objects.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface SiteContext extends BaseContext {
    /** What is the site to use.
     * @return the site.
     **/
    Site getSite();
}
