// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.view.velocity;

import java.io.IOException;
import java.net.URL;
import java.io.InputStream;
import no.schibstedsok.searchportal.http.HTTPClient;
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
public class URLResourceLoader extends ResourceLoader {
    
    public interface Context{
        boolean doesUrlExist(final String url, final String hostHeader);
        String getURL(final String resource);
        String getHostHeader(final String resource);
    }

    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(URLResourceLoader.class);

    private static final String ERR_RESOURCE_NOT_FOUND = "Cannot find resource ";
    private static final String DEBUG_LOOKING_FOR = "Looking for ";
    private static final String DEBUG_EXISTS = "Positive HEAD on ";
    private static final String DEBUG_FULL_URL_IS = "Real URL is ";
    private static final String DEBUG_HOST_HEADER_IS = "URL's host-header is ";
    private static final String DEBUG_DOESNT_EXIST = "Using fallback URL";

    
    // Attributes ----------------------------------------------------
    
    private Site site;

    
    // Static --------------------------------------------------------
    
    private static Context context = new DefaultContext();
    
    // Allows the tests to switch the Velocity ResourceLoader over to a file based one.
    static void setContext(final Context context){
        URLResourceLoader.context = context;
    }
    
    // Constructors --------------------------------------------------
    
    
    // Public --------------------------------------------------------
    
    /** {@inheritDoc}
     */
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
     public /*synchronized*/ InputStream getResourceStream(final String url) throws ResourceNotFoundException{

        LOG.trace("start getResourceStream( " + url + " )");
        try{
            synchronized( url.intern() ){
                return getStream(findUrl(url, site));
            }

        }catch( IOException e ){
            LOG.debug( ERR_RESOURCE_NOT_FOUND + url);
            throw new ResourceNotFoundException( ERR_RESOURCE_NOT_FOUND + url );
        }
    }

    /** {@inheritDoc}
     */
    public boolean isSourceModified(Resource resource){

        final boolean result =  getLastModified(resource) > resource.getLastModified();
        LOG.debug("isSourceModified( "+resource.getName()+" ): "+result);
        return result;
    }

    /** {@inheritDoc}
     */
    public long getLastModified(Resource resource){

        try{

            final String url = findUrl(resource.getName(), site);
            final URL u = new URL(url);

            if( LOG.isTraceEnabled() ){
                LOG.trace(DEBUG_FULL_URL_IS + u);
                LOG.trace(DEBUG_HOST_HEADER_IS + context.getHostHeader(url));
            }

            final HTTPClient client = HTTPClient.instance(
                    u.getProtocol() + "://" + u.getHost(), 
                    u.getPort(), 
                    context.getHostHeader(url));

            return client.getLastModified(u.getPath());

        }catch( ResourceNotFoundException e ){
            LOG.error( ERR_RESOURCE_NOT_FOUND + resource.getName() );
        }catch( IOException e ){
            LOG.error( ERR_RESOURCE_NOT_FOUND + resource.getName() );
        }
        return 0;

    }

    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    private static String findUrl(final String url, final Site currentSite) throws ResourceNotFoundException{

        try{
            LOG.trace(DEBUG_LOOKING_FOR + url );
            return findUrlImpl(url, currentSite);
            
        }catch( IOException e ){
            LOG.error( ERR_RESOURCE_NOT_FOUND + url, e );
            throw new ResourceNotFoundException( ERR_RESOURCE_NOT_FOUND + url );
        }
    }

    private static String findUrlImpl(final String url, final Site currentSite)
            throws IOException, ResourceNotFoundException {

        if (context.doesUrlExist(context.getURL(url),context.getHostHeader(url))) {
            return url;
        } else {
            final Site parent = currentSite.getParent();

            if (null == parent) {
                throw new ResourceNotFoundException( ERR_RESOURCE_NOT_FOUND + url );
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace(DEBUG_DOESNT_EXIST + parent.getName());
            }

            // Recursively look for the resource in ancestor sites.
            return findUrlImpl(getFallbackURL(url, currentSite, parent), parent);
        }
    }

    private static String getFallbackURL(final String url, final Site currSite, final Site ancestorSite) {

        final String oldUrl = currSite.getName() + currSite.getConfigContext();
        final String newUrl = ancestorSite.getName() + ancestorSite.getConfigContext();

        return url.replaceFirst(oldUrl, newUrl);
    }


    private static InputStream getStream(final String url) throws IOException{

        LOG.trace(DEBUG_EXISTS + url);
        final URL u = new URL(context.getURL(url));
        
        if( LOG.isTraceEnabled() ){
            LOG.trace(DEBUG_FULL_URL_IS + u);
            LOG.trace(DEBUG_HOST_HEADER_IS + context.getHostHeader(url));
        }
        
        final HTTPClient client = HTTPClient.instance(
                u.getProtocol() + "://" +u.getHost(), 
                u.getPort(), 
                context.getHostHeader(url));
        
        try{
            return client.getBufferedStream(u.getPath());
            
        }catch(IOException ioe){
            throw client.interceptIOException(ioe);
        }
    }
    
    
    // Inner classes -------------------------------------------------
    
    private static final class DefaultContext implements Context{
        
        public boolean doesUrlExist(final String url, final String hostHeader) {
            return UrlResourceLoader.doesUrlExist(url, hostHeader);
        }

        public String getURL(final String resource) {
            return UrlResourceLoader.getURL(resource);
        }

        public String getHostHeader(final String resource) {
            return UrlResourceLoader.getHostHeader(resource);
        }
    }
}

