/* Copyright (2005-2006) Schibsted Søk AS
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.schibstedsok.searchportal.site.config;


import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.http.HTTPClient;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;

/** Loads resources through URL references.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class UrlResourceLoader extends AbstractResourceLoader {

    // Constants -----------------------------------------------------

    private static final GeneralCacheAdministrator PRESENCE_CACHE = new GeneralCacheAdministrator();   
    private static final int REFRESH_PERIOD = 60; // one minute
    
    private static final Logger LOG = Logger.getLogger(UrlResourceLoader.class);

    private static final String DEBUG_CHECKING_EXISTANCE_OF = "Checking existance of ";


    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    /** Create a new PropertiesLoader for the given resource name/path and load it into the given properties.
     * @param siteCxt the SiteContext that will tell us which site we are dealing with.
     * @param resource the resource name/path.
     * @param properties the properties to hold the individual properties loaded.
     * @return the new PropertiesLoader to use.
     **/
    public static PropertiesLoader newPropertiesLoader(
            final SiteContext siteCxt,
            final String resource,
            final Properties properties) {

        final PropertiesLoader pl = new UrlResourceLoader(siteCxt);
        pl.init(resource, properties);
        return pl;
    }

    /** Create a new DocumentLoader for the given resource name/path and load it with the given DocumentBuilder.
     * @param siteCxt the SiteContext that will tell us which site we are dealing with.
     * @param resource the resource name/path.
     * @param builder the DocumentBuilder to build the DOM resource with.
     * @return the new DocumentLoader to use.
     **/
    public static DocumentLoader newDocumentLoader(
            final SiteContext siteCxt,
            final String resource,
            final DocumentBuilder builder) {

        final DocumentLoader dl = new UrlResourceLoader(siteCxt);
        builder.setEntityResolver(new LocalEntityResolver());
        dl.init(resource, builder);
        return dl;
    }

    /**
     * Creates new BytecodeLoader for the given site and resource.
     *
     * @param siteCxt context telling us which site to use.
     * @param resource the class to load bytecode for.
     * @return a bytecode loader for resource.
     */
    public static BytecodeLoader newBytecodeLoader(final SiteContext siteCxt, final String resource) {
        final BytecodeLoader bcLoader = new UrlResourceLoader(siteCxt);
        bcLoader.initBytecodeLoader(resource);
        return bcLoader;
    }

    public static boolean doesUrlExist(final String url, final String hostHeader){

        boolean success = false;
        
        try{
            success = (Boolean)PRESENCE_CACHE.getFromCache(url, REFRESH_PERIOD);
            
        }catch(NeedsRefreshException nre){
           
            boolean updatedCache = false;
            HttpURLConnection con = null;
            try {

                final URL u = new URL(url);

                con = (HttpURLConnection) u.openConnection();
                con.setInstanceFollowRedirects(false);
                con.setRequestMethod("HEAD");
                con.addRequestProperty("host", hostHeader);
                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);
                
                success = HttpURLConnection.HTTP_OK == con.getResponseCode();
                PRESENCE_CACHE.putInCache(url, success);
                updatedCache = true;

                LOG.trace(DEBUG_CHECKING_EXISTANCE_OF + u + " is " + success);

            } catch (NullPointerException e) {
                LOG.debug( '[' + hostHeader + "] " + url, e);

            } catch (SocketTimeoutException ste) {
                LOG.debug( '[' + hostHeader + "] " + url + '\n' + ste);

            } catch (IOException e) {
                LOG.warn( '[' + hostHeader + "] " + url, e);

            }  finally  {
                
                if(!updatedCache){ 
                    PRESENCE_CACHE.cancelUpdate(url);
                }
                if (con != null) {
                    con.disconnect();
                }
            }
        }

        return success;
    }

    public static String getURL(final String resource){

        return "http://localhost"+
                resource.substring(resource.indexOf(':',8)>0 ? resource.indexOf(':',8) : resource.indexOf('/',8));
    }

    public static String getHostHeader(final String resource){

        return resource.substring(7,resource.indexOf('/',8));
    }


    // Constructors --------------------------------------------------

    /** {@inheritDoc}
     */
    protected UrlResourceLoader(final SiteContext cxt) {
        super(cxt);
    }


    // Public --------------------------------------------------------


    @Override
    public final boolean urlExists(final String url) {

        return doesUrlExist(getUrlFor(url), getHostHeaderFor(url));
    }


    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    @Override
    protected final String getResource(final Site site) {

        return "http://"
                + site.getName()
                + site.getConfigContext()
                + (getResource().endsWith(".class") ? "classes/" : "conf/")
                + getResource();
    }

    protected final String getHostHeaderFor(final String resource){

        return getHostHeader(resource);
    }

    @Override
    protected String getUrlFor(final String resource){

        return getURL(resource);
    }

    @Override
    protected final InputStream getInputStreamFor(String resource) {

        HTTPClient client = null;
        try {
            final URL u = new URL(getUrlFor(resource));
            client = HTTPClient.instance(u.getHost(), u.getPort(), getHostHeaderFor(resource));
            
            return client.getBufferedStream(u.getPath());

        }catch (IOException ex) {
            throw new ResourceLoadException(ex.getMessage(), null != client ? client.interceptIOException(ex) : ex);
        }


    }

    protected final String readResourceDebug(final String resource){

        return "Read Configuration from " + getUrlFor(resource) + " [" + getHostHeaderFor(resource) + ']';
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}