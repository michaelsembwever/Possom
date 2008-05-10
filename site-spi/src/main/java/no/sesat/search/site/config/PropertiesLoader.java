/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 * PropertiesLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 */

package no.sesat.search.site.config;

import java.util.Properties;



/** ResourceLoader to deal with properties resources.
 * @version $Id$
 *
 */
public interface PropertiesLoader extends ResourceLoader {
    /** initialise this resource loader with the resource name/path and the resource it will go into.
     * existing properties will not be overwritten.
     *
     *@param resource the name/path of the resource.
     *@param properties the properties that will be used to hold the individual properties.
     **/
    void init(String resource, Properties properties);
    /** get the properties.
     *@return the properties.
     **/
    Properties getProperties();

}
