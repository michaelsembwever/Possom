package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;
import no.schibstedsok.searchportal.site.config.UrlResourceLoader;
import no.schibstedsok.searchportal.site.Site;
import org.apache.log4j.Logger;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.exception.ResourceNotFoundException;

import org.apache.commons.collections.ExtendedProperties;

/** XXX This source file needs to be published to the internet as it is open-source code.
 *
 *
 * This is a simple URL-based loader.
 * ORIGINAL FROM http://svn.apache.org/repos/asf/jakarta/velocity/engine/trunk/whiteboard/geir/URLResourceLoader.java
 *
 * original version Id: URLResourceLoader.java,v 1.3 2004/03/19 17:13:40 dlr Exp
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 *
 *
 * MODIFIED TO SUIT SCHIBSTEDSØK's NEEDS.
 * There was a choice here to implement all the URL handling stuff from scratch or to plug into the existing
 * functionality found in no.schibstedsøk.front.searchportal.configuration.loader
 * Since this class is hidden between the velocity API it made more sense to go from scratch to best
 * meet velocity's requirements...
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: URLResourceLoader.java,v 1.3 2004/03/19 17:13:40 dlr Exp $
 */
public final class URLVelocityTemplateLoader extends ResourceLoader {

    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(URLVelocityTemplateLoader.class);

    private static final String ERR_RESOURCE_NOT_FOUND = "Cannot find resource ";
    private static final String DEBUG_LOOKING_FOR = "Looking for ";
    private static final String DEBUG_EXISTS = "Positive HEAD on ";
    private static final String DEBUG_FULL_URL_IS = "Real URL is ";
    private static final String DEBUG_HOST_HEADER_IS = "URL's host-header is ";
    private static final String DEBUG_DOESNT_EXIST = "Using fallback URL";

    
    // Attributes ----------------------------------------------------
    
    private Site site;

    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    
    // Public --------------------------------------------------------
    
    /** {@inheritDoc}
     */
    @Override
    public void init(final ExtendedProperties configuration) {
        // the engine's properties actually come from the RuntimeServices *not* the ExtendedProperties
        site = (Site)rsvc.getProperty(Site.NAME_KEY);
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param url  url of template to fetch bytestream of
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     */
    @Override
    public synchronized InputStream getResourceStream( final String url )
        throws ResourceNotFoundException{

        LOG.trace("start getResourceStream( " + url + " )");
        try{

            final URLConnection conn = getResourceURLConnection(url);
            return conn.getInputStream();

        }catch( IOException e ){
            LOG.debug( ERR_RESOURCE_NOT_FOUND + url);
            throw new ResourceNotFoundException( ERR_RESOURCE_NOT_FOUND + url );
        }



    }

    /** {@inheritDoc}
     */
    @Override
    public boolean isSourceModified(Resource resource){

        final boolean result =  getLastModified(resource) > resource.getLastModified();
        LOG.debug("isSourceModified( "+resource.getName()+" ): "+result);
        return result;
    }

    /** {@inheritDoc}
     */
    @Override
    public long getLastModified(Resource resource){

        final String url = resource.getName();
        LOG.trace("start getLastModified( "+url+" )");
        try{

            final URLConnection conn = getResourceURLConnection(url);
            return conn.getLastModified();

        }catch( ResourceNotFoundException e ){
            LOG.error( ERR_RESOURCE_NOT_FOUND + url );
        }
        return 0;

    }

    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    private URLConnection getResourceURLConnection(final String url)
            throws ResourceNotFoundException{

        try{
            LOG.trace(DEBUG_LOOKING_FOR + url );
            return doGetResourceURLConnection(url, site);
        }catch( IOException e ){
            LOG.error( ERR_RESOURCE_NOT_FOUND + url, e );
            throw new ResourceNotFoundException( ERR_RESOURCE_NOT_FOUND + url );
        }
    }

    private URLConnection doGetResourceURLConnection(
            final String url, 
            final Site currentSite)
                throws IOException, ResourceNotFoundException {

        if (UrlResourceLoader.doesUrlExist(UrlResourceLoader.getURL(url))) {
            return getURLConnection(url);
            
        } else {
            final Site parent = currentSite.getParent();

            if (parent == null) {
                throw new ResourceNotFoundException( ERR_RESOURCE_NOT_FOUND + url );
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace(DEBUG_DOESNT_EXIST + parent.getName());
            }

            // Recursively look for the resource in ancestor sites.
            return doGetResourceURLConnection(getFallbackURL(url, currentSite, parent), parent);
        }
    }

    private String getFallbackURL(final String url, final Site currSite, final Site ancestorSite) {

        final String oldUrl = currSite.getName() + currSite.getConfigContext();
        final String newUrl = ancestorSite.getName() + ancestorSite.getConfigContext();

        return url.replaceFirst(oldUrl, newUrl);
    }


    private URLConnection getURLConnection(final String url) throws IOException{

        // TODO make this loopback to call a context's ResourceLoader method from the site-spi.
        LOG.trace(DEBUG_EXISTS + url);
        final URL u = new URL( UrlResourceLoader.getURL(url) );
        
        LOG.trace(DEBUG_FULL_URL_IS + u);
        final URLConnection conn = u.openConnection();
        final String hostHeader = UrlResourceLoader.getHostHeader(url);
        
        LOG.trace(DEBUG_HOST_HEADER_IS + hostHeader);
        conn.addRequestProperty("host", hostHeader);
        
        return conn;
    }
    
    
    // Inner classes -------------------------------------------------
    
}

