/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * UrlResourceLoader.java
 *
 * Created on 20 January 2006, 10:24
 *
 */

package no.sesat.search.site.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
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

    /**
     * Creates new BytecodeLoader for the given site and resource.
     *
     * @param siteCxt context telling us which site to use.
     * @param resource the class to load bytecode for.
     * @return a bytecode loader for resource.
     */
    public static BytecodeLoader newBytecodeLoader(final SiteContext siteCxt, final String resource, final String jar) {
        final BytecodeLoader bcLoader = new FileResourceLoader(siteCxt);
        bcLoader.initBytecodeLoader(resource, jar);
        return bcLoader;
    }

    /** {@inheritDoc}
     */
    protected FileResourceLoader(final SiteContext cxt) {
        super(cxt);
    }

    @Override
    public boolean urlExists(final URL url) {

        try {
            return new File(url.toURI()).exists();
        }catch (URISyntaxException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return false;
    }

     protected final String getProjectName(final String siteName){
        // Very hacky.
        String projectName = siteName.replaceAll("localhost", "sesam");
        if( projectName.indexOf(':') > 0 ){
            projectName = projectName.substring(0, projectName.indexOf(':'));
        }
        if( !projectName.endsWith("/")){
            projectName = projectName + '/';
        }
        if( projectName.endsWith("sesam/") && !"generic.sesam/".equals(projectName) ){
            projectName = projectName.substring(0, projectName.length() - 1) + ".no/";
        }

        if( "catalogue/".equals(projectName)){
            projectName = "katalog.sesam.no/";
        }
        return projectName;
    }

    @Override
    protected final URL getResource(final Site site) {
         
        LOG.debug("getResource(" + site + ')');

        try{
            final String base = System.getProperty("basedir") // test jvm sets this property
                    + (System.getProperty("basedir").endsWith("war") ? "/../../" : "/../")
                    + getProjectName(site.getName());

            final File warFolder = new File(base + "/war");

            URI uri = new URI("file://"
                    + base
                    + (warFolder.exists() && warFolder.isDirectory() ? "/war/target/" : "/target/") + getResourceDirectory()
                    + getResource()).normalize();

            return uri.toURL();
        }catch (URISyntaxException ex) {
            throw new ResourceLoadException(ex.getMessage(), ex);
        } catch (MalformedURLException ex) {
            throw new ResourceLoadException(ex.getMessage(), ex);
        }
    }

    private String getResourceDirectory() {
        if (getResource().contains("jar!")) {
            return "lib/";
        } else {
            return "classes/";
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

    @Override
    protected InputStream getInputStreamFor(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            throw new ResourceLoadException(e.getMessage(), e);
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
