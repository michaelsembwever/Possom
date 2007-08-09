/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 * PropertiesLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 */

package no.schibstedsok.searchportal.site.config;

import java.util.Properties;



/** ResourceLoader to deal with properties resources.
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
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
