/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * Created on 23 January 2006, 13:54
 */

package no.sesat.search.site.config;

import java.util.Properties;
import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.search.site.SiteContext;

/** Defines the context for consumers of PropertiesLoaders.
 *
 * @version $Id: ResourceContext.java 2045 2006-01-25 12:10:01Z mickw $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface PropertiesContext extends BaseContext {
    /** Create a new PropertiesLoader for the given resource name/path and load it into the given properties.
     * Will not overwrite existing properties.
     *
     * @param resource the resource name/path.
     * @param properties the properties to hold the individual properties loaded.
     * @return the new PropertiesLoader to use.
     **/
    PropertiesLoader newPropertiesLoader(SiteContext siteCxt, String resource, Properties properties);
}
