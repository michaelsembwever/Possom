/*
 * SiteClassLoader.java
 *
 * Created on 20 December 2006, 16:22
 *
 */

package no.schibstedsok.searchportal.site.config;


import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author mick
 */
final class SiteClassLoader extends URLClassLoader{
    
    /** Creates a new instance of UrlClassLoader. **/
    public SiteClassLoader(final URL url, final ClassLoader parent){
        super(new URL[]{url}, parent);
    }
    
}
