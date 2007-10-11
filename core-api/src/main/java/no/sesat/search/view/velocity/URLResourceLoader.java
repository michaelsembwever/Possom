/* Copyright (2007) Schibsted Søk AS
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
 */
package no.sesat.search.view.velocity;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import no.sesat.search.http.HTTPClient;
import no.sesat.search.site.config.UrlResourceLoader;
import no.sesat.search.site.Site;
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
 * functionality found in no.schibstedsøk.front.search.configuration.loader
 * Since this class is hidden between the velocity API it made more sense to go from scratch to best
 * meet velocity's requirements...
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: URLResourceLoader.java,v 1.3 2004/03/19 17:13:40 dlr Exp $
 */
public class URLResourceLoader extends ResourceLoader {
    
    public interface Context{
        boolean doesUrlExist(final URL url);
        URL getURL(final String resource, final Site site);
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
            final URL url = findUrl(resource.getName(), site);
            final HTTPClient client = HTTPClient.instance(url, "localhost");

            return client.getLastModified("");
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
    
    private static URL findUrl(final String url, final Site currentSite) throws ResourceNotFoundException{

        try{
            LOG.trace(DEBUG_LOOKING_FOR + url );
            return findUrlImpl(url, currentSite);
            
        }catch( IOException e ){
            LOG.error( ERR_RESOURCE_NOT_FOUND + url, e );
            throw new ResourceNotFoundException( ERR_RESOURCE_NOT_FOUND + url );
        }
    }

    private static URL findUrlImpl(final String url, final Site currentSite)
            throws IOException, ResourceNotFoundException {

        final URL u = context.getURL(url, currentSite);

        if (context.doesUrlExist(u)) {
            return u;
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


    private static InputStream getStream(final URL url) throws IOException{

        final HTTPClient client = HTTPClient.instance(url, "localhost");
        try{
            return client.getBufferedStream("");
        }catch(IOException ioe){
            throw client.interceptIOException(ioe);
        }
    }
    
    
    // Inner classes -------------------------------------------------

    private static final class DefaultContext implements Context{
        public boolean doesUrlExist(final URL url) {
            return UrlResourceLoader.doesUrlExist(url);
        }

        public URL getURL(final String resource, final Site site) {
            try {
                return new URL(resource);
            } catch (MalformedURLException e) {
                throw new ResourceNotFoundException(e); 
            }
        }
    }
}

