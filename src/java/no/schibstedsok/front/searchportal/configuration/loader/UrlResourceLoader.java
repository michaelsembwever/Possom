/* Copyright (2005-2006) Schibsted Søk AS
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.schibstedsok.front.searchportal.configuration.loader;

import com.thoughtworks.xstream.XStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;
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

    /** Create a new XStreamLoader for the given resource name/path and load it with the given XStream.
     * @param siteCxt the SiteContext that will tell us which site we are dealing with.
     * @param resource the resource name/path.
     * @param xstream the xstream to deserialise the resource with.
     * @return the new PropertiesLoader to use.
     **/
    public static XStreamLoader newXStreamLoader(
            final SiteContext siteCxt,
            final String resource,
            final XStream xstream) {

        final XStreamLoader xl = new UrlResourceLoader(siteCxt);
        xl.init(resource, xstream);
        return xl;
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
        dl.init(resource, builder);
        return dl;
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
    
    private String getHostHeader(final String resource){
        return resource.substring(7,resource.indexOf('/',8));
    }
    
    private String getURL(final String resource){
        return "http://localhost"+
                resource.substring( resource.indexOf(':',8)>0 ? resource.indexOf(':',8) : resource.indexOf('/',8));
    }

    /** {@inheritDoc}
     */
    protected String getFallbackResource() {
        final Site fallback = Site.DEFAULT;

        return "http://"
                + fallback.getName()
                + fallback.getConfigContext()
                + "conf/"
                + super.getResource();
    }

    /** {@inheritDoc}
     */
    public void run() {
        if (!loadResource(getResource())) {
            LOG.warn(WARN_USING_FALLBACK + getResource());
            if (!loadResource(getFallbackResource())) {
                LOG.fatal(FATAL_RESOURCE_NOT_LOADED);
            }
        }

    }

    private boolean loadResource(final String resource) {

        boolean success = false;
        try {

            final URLConnection urlConn = new URL(getURL(resource)).openConnection();
            urlConn.addRequestProperty("host", getHostHeader(resource));
            

            if (props != null) {
                props.load(urlConn.getInputStream());
            }
            if (xstream != null) {
                xstreamResult = xstream.fromXML(new InputStreamReader(urlConn.getInputStream()));
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

        return success;
    }

}
