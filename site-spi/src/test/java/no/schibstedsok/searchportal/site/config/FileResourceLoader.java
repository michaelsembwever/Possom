/* Copyright (2005-2006) Schibsted Søk AS
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.schibstedsok.searchportal.site.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/** Loads resource through ClassLoader resources.
 *
 * @version $Id: FileResourceLoader.java 3361 2006-08-03 13:44:54Z mickw $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class FileResourceLoader extends AbstractResourceLoader {

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
        builder.setEntityResolver(new LocalTestEntityResolver());
        dl.init(resource, builder);
        return dl;
    }

    /** {@inheritDoc}
     */
    protected FileResourceLoader(final SiteContext cxt) {
        super(cxt);
    }

    public boolean urlExists(final String url) {
        
        try {
            final URL u = new URL(getUrlFor(url));

            return new File(u.toURI()).exists();
            
        }catch (URISyntaxException ex) {
            LOG.error(ex.getMessage(), ex);
            
        }catch (MalformedURLException ex) {
            LOG.error(ex.getMessage(), ex);
            
        }

        return false;
    }
    
    protected final String getProjectName(final String siteName){
        
        LOG.info("getProjectName(" + siteName + ')');
        // Very hacky.
        String projectName = siteName.replaceAll("localhost", "sesam");
        if( projectName.indexOf(':') > 0 ){
            projectName = projectName.substring(0, projectName.indexOf(':'));
        }
        if( projectName.endsWith("sesam") && !"generic.sesam".equals(projectName) ){
            projectName = projectName + ".no";
        }
        if( "catalogue".equals(projectName)){
            projectName = "katalog.sesam.no";
        }
        LOG.info("result: " + projectName);
        return projectName;
    }

    protected String getResource(final Site site) {
        
        LOG.debug("getResource(" + site + ')');
        
        try{
            final String base = System.getProperty("basedir") // test jvm sets this property
                    + (System.getProperty("basedir").endsWith("war") ? "/../../" : "/../")
                    + getProjectName(site.getName());

            final File warFolder = new File(base + "/war");

            final String result = new URI("file://"
                    + base 
                    + (warFolder.exists() && warFolder.isDirectory() ? "/war/target/classes/" : "/target/classes/")
                    + getResource()).normalize().toString();

            LOG.debug("result: " + result);
            return result;
         
        }catch (URISyntaxException ex) {
            throw new ResourceLoadException(ex.getMessage(), ex);
        } 
    }

    protected String getUrlFor(final String resource) {
        
//        LOG.debug("getUrlFor(" + resource + ')');
//        final String result = "file://"
//                + System.getProperty("basedir") // test jvm sets this property
//                + "/target/classes/"
//                + resource;
//        LOG.debug("result: " + result);
        return resource;
    }

    protected String getHostHeaderFor(final String resource) {
        
        return "localhost";
    }

    protected InputStream getInputStreamFor(String resource) {

        try {
            return new FileInputStream(resource.replaceFirst("file:", ""));
        }catch (FileNotFoundException ex) {
            throw new ResourceLoadException(ex.getMessage(), ex);
        }
    }

    private static class LocalTestEntityResolver implements EntityResolver {

        private static final Logger LOG = Logger.getLogger(LocalTestEntityResolver.class);
        private static final String INFO_LOADING_DTD = "Loading local DTD ";


        public InputSource resolveEntity(final String publicId, final String systemId) {

            // the latter is only for development purposes when dtds have't been published to production yet
            if (systemId.startsWith("http://sesam.no/dtds/") || systemId.startsWith("http://localhost")) {
                
                final String dtd = System.getProperty("basedir") // test jvm sets this property
                    + (System.getProperty("basedir").endsWith("war") ? "/../../" : "/../")
                    + "search-portal/war/src/webapp/dtds/" +
                    systemId.substring(systemId.lastIndexOf('/'));

                LOG.info(INFO_LOADING_DTD + dtd);
                try{
                    return new InputSource(new FileInputStream(dtd));
                    
                }catch (FileNotFoundException ex) {
                    throw new ResourceLoadException(ex.getMessage(), ex);
                }
                
            } else {
                // use the default behaviour
                return null;
            }
        }

    }
}
