/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Created on 23 January 2006, 13:54
 */

package no.schibstedsok.searchportal.site.config;

import java.util.Properties;
import no.schibstedsok.common.ioc.BaseContext;

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
    PropertiesLoader newPropertiesLoader(String resource, Properties properties);
}
