/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Created on 23 January 2006, 13:54
 */

package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.site.SiteContext;

/** Defines the context for consumers of ClasspathLoaders.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface ClasspathContext extends BaseContext {
    /** Create a new ClasspathLoader for the given skin.
     *
     * @return the new ClasspathLoader to use.
     **/
    ClasspathLoader newClasspathLoader(SiteContext siteCxt, String resource);
}
