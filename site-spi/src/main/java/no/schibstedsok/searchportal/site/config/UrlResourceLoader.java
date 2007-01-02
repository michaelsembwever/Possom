/* Copyright (2005-2006) Schibsted Søk AS
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.schibstedsok.searchportal.site.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;

/** Loads resources through URL references.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class UrlResourceLoader extends AbstractResourceLoader {

    private static final Logger LOG = Logger.getLogger(UrlResourceLoader.class);

    private static final String DEBUG_CHECKING_EXISTANCE_OF = "Checking existance of ";

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
    
    public static ClasspathLoader newClassLoader(
            final SiteContext siteCxt,
            final ClassLoader parent){
        
        final ClasspathLoader cl = new UrlResourceLoader(siteCxt);
        cl.init("src/", parent);
        return cl;
    }
    
    public static boolean doesUrlExist(final String url){
        
        boolean success = false;
        HttpURLConnection con = null;
        try {

            final URL u = new URL(getURL(url));

            con = (HttpURLConnection) u.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("HEAD");
            con.addRequestProperty("host", getHostHeader(url));
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            success = HttpURLConnection.HTTP_OK == con.getResponseCode();

            LOG.trace(DEBUG_CHECKING_EXISTANCE_OF + u + " is " + success);

        } catch (NullPointerException e) {
            LOG.debug( '[' + getURL(url) + "] " + url, e);

        } catch (SocketTimeoutException ste) {
            LOG.debug( '[' + getURL(url) + "] " + url + '\n' + ste);

        } catch (IOException e) {
            LOG.warn( '[' + getURL(url) + "] " + url, e);
            
        }  finally  {
            if (con != null) {
                con.disconnect();
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

    /** {@inheritDoc}
     */
    protected UrlResourceLoader(final SiteContext cxt) {
        super(cxt);
    }

    protected final String getResource(final Site site) {
        
        return "http://"
                + site.getName()
                + site.getConfigContext()
                + "conf/"
                + getResource();
    }
   
    public final boolean urlExists(final String url) {

        return doesUrlExist(url);
    }
    
    protected final String getHostHeaderFor(final String resource){
        
        return getHostHeader(resource);
    }

    protected String getUrlFor(final String resource){
        
        return getURL(resource);
    }

    protected final InputStream getInputStreamFor(String resource) {
        
        try {
            final URLConnection urlConn = new URL(getUrlFor(resource)).openConnection();

            urlConn.addRequestProperty("host", getHostHeaderFor(resource));
            return urlConn.getInputStream();
            
        }catch (IOException ex) {
            throw new ResourceLoadException(ex.getMessage(), ex);
        }
         
        
    }
    
    protected final String readResourceDebug(final String resource){
        
        return "Read Configuration from " + getUrlFor(resource) + " [" + getHostHeaderFor(resource) + ']';
    }

}