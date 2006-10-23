/* Copyright (2005-2006) Schibsted Søk AS
 * XStreamLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
