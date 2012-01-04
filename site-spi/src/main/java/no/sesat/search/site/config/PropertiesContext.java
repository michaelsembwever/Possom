/* Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created on 23 January 2006, 13:54
 */

package no.sesat.search.site.config;

import java.util.Properties;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.search.site.SiteContext;

/** Defines the context for consumers of PropertiesLoaders.
 *
 * @version $Id: ResourceContext.java 2045 2006-01-25 12:10:01Z mickw $
 *
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
