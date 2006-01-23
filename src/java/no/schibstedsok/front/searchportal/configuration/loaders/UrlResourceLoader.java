/* Copyright (2005-2006) Schibsted Søk AS
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.schibstedsok.front.searchportal.configuration.loaders;

import com.thoughtworks.xstream.XStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.site.SiteContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Loads resources through URL references.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class UrlResourceLoader extends AbstractResourceLoader {

    private static final Log LOG = LogFactory.getLog(UrlResourceLoader.class);

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

    /** {@inheritDoc}
     */
    private UrlResourceLoader(final SiteContext cxt) {
        super(cxt);
    }

    /** {@inheritDoc}
     */
    protected String getResource() {
        // the web context (eg "sesam.no") must come from the site object.
        final String vhost = getContext().getSite().getSiteName().endsWith("/")
            ? getContext().getSite().getSiteName()
            : getContext().getSite().getSiteName() + "/";

        final String cxtName = vhost.indexOf(':') >= 0
            ? vhost.substring(0, vhost.indexOf(':')) + "/" // don't include the port in the cxtName.
            : vhost;

        return "http://" + vhost + cxtName + "conf/" + super.getResource();
    }

    /** {@inheritDoc}
     */
    public void run() {
         try {

            final URL url = new URL(getResource());

            if (props != null) {
                props.load(url.openStream());
            }
            if (xstream != null) {
                xstreamResult = xstream.fromXML(new InputStreamReader(url.openStream()));
            }

            LOG.info("Read configuration from " + getResource());

        } catch (NullPointerException e) {
            final String err = "When Reading Configuration from " + getResource();
            LOG.error(err, e);
            throw new InfrastructureException(err, e);

        } catch (IOException e) {
            final String err = "When Reading Configuration from " + getResource();
            LOG.error(err, e);
            throw new InfrastructureException(err, e);
        }
    }


}
