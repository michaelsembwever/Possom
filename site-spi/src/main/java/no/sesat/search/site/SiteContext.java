/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * SiteContext.java
 *
 * Created on 23 January 2006, 13:55
 *
 */

package no.sesat.search.site;

import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.search.site.config.BytecodeLoader;

/** Defines the context for consumers of Site objects.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface SiteContext extends BaseContext {
    /** What is the site to use.
     *<b>If you override this then you must also override any ResourceContexts!!</b>
     * @return the site.
     **/
    Site getSite();
}
