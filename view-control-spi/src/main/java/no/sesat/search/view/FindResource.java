/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * FindResource.java
 * 
 * Created on 16/09/2007, 09:37:35
 * 
 */

package no.sesat.search.view;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import no.sesat.search.site.Site;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.config.UrlResourceLoader;
import org.apache.log4j.Logger;

/**
 *
 * @author mick
 * @version $Id$
 */
public final class FindResource {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FindResource.class);

    private static final String HTTP = "http://";
    private static final String PUBLISH_DIR = "/img/";
    
    public static final long START_TIME = System.currentTimeMillis();
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    public static String find(final Site site, final String resource) throws IOException{

        // This URL does not belong to search-portal
        final String url;

        if (resource.startsWith(PUBLISH_DIR)) { // publishing system
            // the publishing system is responsible for this.
            final Properties props = SiteConfiguration.valueOf(site).getProperties();
            url = props.getProperty(SiteConfiguration.PUBLISH_SYSTEM_URL)
                + '/' + resource;

        }  else  {
            // strip the version number out of the resource
            final String noVersionRsc = resource.replaceFirst("/(\\d)+/","/");

            // Find resource in current site or any of its
            // ancestors
            url = recursivelyFindResource(noVersionRsc, site);

        }

        return url;
    }

    // Constructors --------------------------------------------------

    private FindResource() {
    }

    // Public --------------------------------------------------------
    
    // Z implementation ----------------------------------------------
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    private static String recursivelyFindResource(final String resource, final Site site) throws IOException {

        // Problem with this approach is that skins can be updated without the server restarting (& updating START_TIME)
        // TODO an alternative approach would be to collect the lastModified timestamp of the resource and use it.
        final String datedResource = resource
                .replaceAll("/", "/" + START_TIME + "/")
                .replaceFirst("/" + START_TIME + "/", "");

        final String url = HTTP + site.getName() + site.getConfigContext() + '/' + datedResource;

        final URL u = new URL(url);

        if (UrlResourceLoader.doesUrlExist(u)) {
            // return a relative url to ensure it can survice through an out-of-cluster server.
            return '/' + site.getConfigContext() + '/' + datedResource;
            
        } else if (site.getParent() != null) {
            return recursivelyFindResource(resource, site.getParent());
            
        } else {
            return null;
        }
    }

    // Inner classes -------------------------------------------------
}
