/* Copyright (2005-2006) Schibsted Søk AS
 * ClasspathLoader.java
 *
 * Created on 23 January 2006, 09:45
 *
 */

package no.schibstedsok.searchportal.site.config;


/** ResourceLoader to deal with properties resources.
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface ClasspathLoader extends ResourceLoader {
    
    /** initialise this resource loader.
     *
     *@param resource the name/path of the resource.
     *@param properties the properties that will be used to hold the individual properties.
     **/
    void init(String classpath, ClassLoader parent);
    /** get the properties.
     *@return the properties.
     **/
    ClassLoader getClassLoader();

}
