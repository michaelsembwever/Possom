/* Copyright (2005-2006) Schibsted Søk AS
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.schibstedsok.front.searchportal.configuration.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Loads resource through ClassLoader resources.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class FileResourceLoader extends AbstractResourceLoader {

    private static final Logger LOG = Logger.getLogger(FileResourceLoader.class);

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

        final PropertiesLoader pl = new FileResourceLoader(siteCxt);
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

        final DocumentLoader dl = new FileResourceLoader(siteCxt);
        builder.setEntityResolver(new LocalEntityResolver());
        dl.init(resource, builder);
        return dl;
    }

    /** {@inheritDoc}
     */
    private FileResourceLoader(final SiteContext cxt) {
        super(cxt);
    }

    /** {@inheritDoc}
     */
    protected String getResource() {
        return "/" + super.getResource();
    }

    /** {@inheritDoc}
     */
    public void run() {
         try {

            if (props != null) {
                props.load(getClass().getResourceAsStream(getResource()));
            }
            if (builder != null) {
                document = builder.parse(
                        new InputSource(new InputStreamReader(getClass().getResourceAsStream(getResource()))));
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

        } catch (SAXException e) {
            final String err = "When Reading Configuration from " + getResource();
            LOG.error(err, e);
            throw new InfrastructureException(err, e);
        }
    }

}
