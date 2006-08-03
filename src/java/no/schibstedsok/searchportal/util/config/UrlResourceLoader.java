/* Copyright (2005-2006) Schibsted Søk AS
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.schibstedsok.searchportal.util.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Loads resources through URL references.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class UrlResourceLoader extends AbstractResourceLoader {

    private static final Logger LOG = Logger.getLogger(UrlResourceLoader.class);

    private static final String WARN_USING_FALLBACK = "Falling back to default version for resource ";
    private static final String FATAL_RESOURCE_NOT_LOADED = "Resource not found ";
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

    public static boolean urlExists(final String url) {

        boolean success = false;
        HttpURLConnection con = null;
        try {

            final URL u = new URL(UrlResourceLoader.getURL(url));

            con = (HttpURLConnection) u.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("HEAD");
            con.addRequestProperty("host", UrlResourceLoader.getHostHeader(url));
            success = (con.getResponseCode() == HttpURLConnection.HTTP_OK);

            LOG.trace(DEBUG_CHECKING_EXISTANCE_OF + u + " is " + success);

        } catch (NullPointerException e) {
            LOG.debug(url, e);

        } catch (IOException e) {
            LOG.warn(url, e);
        }  finally  {
            if (con != null) {
                con.disconnect();
            }
        }

        return success;
    }

    /** {@inheritDoc}
     */
    private UrlResourceLoader(final SiteContext cxt) {
        super(cxt);
    }

    /** {@inheritDoc}
     */
    protected String getResource() {

        return "http://"
                + getContext().getSite().getName()
                + getContext().getSite().getConfigContext()
                + "conf/"
                + super.getResource();
    }

    private String getResource(final Site site) {
        
        return "http://"
                + site.getName()
                + site.getConfigContext()
                + "conf/"
                + super.getResource();
    }
    
    public static String getHostHeader(final String resource){
        return resource.substring(7,resource.indexOf('/',8));
    }

    public static String getURL(final String resource){
        return "http://localhost"+
                resource.substring(resource.indexOf(':',8)>0 ? resource.indexOf(':',8) : resource.indexOf('/',8));
    }

    /** {@inheritDoc}
     */
    public void run() {
        if(props != null){
            // Properties inherent through the fallback process. Keys are *not* overridden.

            Site site = getContext().getSite();
            
            do {
                loadResource(getResource(site));
                site = site.getParent();
                
            } while (site != null);
            
        }else{
            // Default behavour: only load first found resource
            if (!loadResource(getResource())) {
                Site site = getContext().getSite();
                LOG.warn(WARN_USING_FALLBACK + getResource());

                do {
                    if (loadResource(getResource(site))) {
                        break;
                    } else {
                        site = site.getParent();
                    }
                } while (site != null);

                if (site == null) {
                    LOG.fatal(FATAL_RESOURCE_NOT_LOADED);
                }
            }
        }
    }

    private boolean loadResource(final String resource) {

        boolean success = false;

        if(urlExists(resource)){

            try {

                final URLConnection urlConn = new URL(getURL(resource)).openConnection();
                urlConn.addRequestProperty("host", getHostHeader(resource));


                if (props != null) {
                    // only add properties that don't already exist!
                    // allows us to inherent back through the fallback process.
                    final Properties newProps = new Properties();
                    newProps.load(urlConn.getInputStream());
                    
                    synchronized( props ){
                        for(Object p : newProps.keySet()){

                            if(!props.containsKey(p)){
                                final String prop = (String)p;
                                props.setProperty(prop, newProps.getProperty(prop));
                            }
                        }
                    }
                }
                if (builder != null) {
                    document = builder.parse(
                            new InputSource(new InputStreamReader(urlConn.getInputStream())));
                }

                LOG.info("Read configuration from " + getURL(resource)+" ["+getHostHeader(resource)+"]");
                success = true;

            } catch (NullPointerException e) {
                final String err = "When Reading Configuration from " + getURL(resource)+" ["+getHostHeader(resource)+"]";
                LOG.warn(err, e);
                //throw new InfrastructureException(err, e);

            } catch (IOException e) {
                final String err = "When Reading Configuration from " + getURL(resource)+" ["+getHostHeader(resource)+"]";
                LOG.warn(err, e);
                //throw new InfrastructureException(err, e);
            } catch (SAXParseException e) {
                final String err = "When Reading Configuration from " + getURL(resource)+" ["+getHostHeader(resource)+"]" +
                        " at " + e.getLineNumber() + ":" + e.getColumnNumber();
                LOG.warn(err, e);
                throw new InfrastructureException(err, e);
            } catch (SAXException e) {
                final String err = "When Reading Configuration from " + getURL(resource)+" ["+getHostHeader(resource)+"]";
                LOG.warn(err, e);
                throw new InfrastructureException(err, e);
            }
        }
        return success;
    }

}