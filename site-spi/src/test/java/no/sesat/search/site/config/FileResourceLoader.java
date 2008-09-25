/* Copyright (2005-2008) Schibsted Søk AS
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
 *
 * FileResourceLoader.java
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
 *
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
     * @param jar
     * @return a bytecode loader for resource.
     */
    public static BytecodeLoader newBytecodeLoader(final SiteContext siteCxt, final String resource, final String jar) {

        final BytecodeLoader bcLoader = new FileResourceLoader(siteCxt);
        bcLoader.initBytecodeLoader(resource, jar);
        return bcLoader;
    }

    protected FileResourceLoader(final SiteContext cxt) {
        super(cxt);
    }

    @Override
    public boolean urlExists(final URL url) {

            return null != url && new File(url.getFile()).exists();
    }

     protected final String getProjectName(final String siteName){

        // XXX Very hacky and awful! desparately needs attention. One idea is to always use "generic.sesam" as skin name
        String projectName = siteName
                .replaceAll("(localhost|(alpha|nuclei|beta|electron|gamma).test.sesam)", "sesam")
                .replaceAll("generic.(sesam.no)", "generic.sesam");

        if( projectName.indexOf(':') > 0 ){
            projectName = projectName.substring(0, projectName.indexOf(':'));
        }

        if( !projectName.endsWith("/")){
            projectName = projectName + '/';
        }

        if( projectName.endsWith("sesam/") && !"generic.sesam/".equals(projectName) ){
            projectName = projectName.substring(0, projectName.length() - 1) + ".no/";
        }else if( !projectName.contains(".") ){
            projectName = projectName.substring(0, projectName.length() - 1) + ".sesam.no/";

        }
        return projectName.replace('/', File.separatorChar);
    }

    // Not a doubt in my mind someone could write this a shit load better. i'll buy a beer to them.
    @Override
    protected final URL getResource(final Site site) {

        LOG.debug("getResource(" + site + ')');

        final String suffix = "target" + File.separatorChar + getResourceDirectory();
        return getResource(suffix, getResource(), site, false);
    }

    protected final URL getResource(final String directory, final String resource, final Site site, final boolean forceUrl){

        LOG.debug("getResource(" + directory + ", " + resource + ", " + site + ")");

        final String project = getProjectName(site.getName());
        int genericSesamLoop = 0;

        try{

            if(resource.contains("jar!") || resource.endsWith(".class")){
                final String classname = resource.substring(resource.indexOf("jar!") + 4);
                // this will actually fail. but the class is loaded eventually cause everything is in one classloader.
                return FileResourceLoader.class.getClassLoader().getResource(classname);
            }

            String basedir = System.getProperty("basedir") + File.separatorChar;
            LOG.debug("project " + project);
            while(true){
                String basedirNormalised = new File(basedir).toURI().normalize().toString()
                        .replaceFirst("file:", "")
                        .replace('/', File.separatorChar);

                if(!basedirNormalised.endsWith(File.separator)){
                    basedirNormalised =  basedirNormalised + File.separatorChar;
                }

                LOG.debug("basedirNormalised " + basedirNormalised);

                if((File.separatorChar + "war" + File.separatorChar).equals(basedirNormalised)
                        || (File.separatorChar + "generic.sesam" + File.separatorChar + "war" + File.separatorChar).equals(basedirNormalised)
                        || (File.separatorChar + "sesat-kernel" + File.separatorChar + "generic.sesam" + File.separatorChar + "war" + File.separatorChar).equals(basedirNormalised)){

                    LOG.warn("At root of filesystem! looking for " + directory + resource
                            + " Current requirement of tests is that sesat-kernel is checked out, and named such,"
                            + " in any parent folder from here."
                            + " I've searched all the way to the root of the filesystem");
                    return null;
                }

                if(basedirNormalised.endsWith(project)
                        || basedirNormalised.endsWith(project + "war" + File.separatorChar)){

                    LOG.debug("looking in " + basedirNormalised + directory);
                    final File f = new File(basedirNormalised + directory + resource);
                    if(f.exists() || forceUrl){
                        return f.toURI().normalize().toURL();
                    }
                }
                if(("generic.sesam" + File.separatorChar).equals(project)){
                    ++genericSesamLoop;
                    basedir = System.getProperty("basedir") + File.separatorChar;
                    for(int i = 0; i < genericSesamLoop; ++i){
                        basedir = basedir + ".." + File.separatorChar;
                    }
                    if(new File(basedir + "generic.sesam").exists()){
                        // we are already inside sesat-kernel
                        basedir = basedir + "generic.sesam" + File.separatorChar + "war" + File.separatorChar;

                    }else{
                        basedir = basedir + "sesat-kernel" + File.separatorChar
                                + "generic.sesam" + File.separatorChar + "war" + File.separatorChar;
                    }
                }else{
                    basedir = basedir
                            + (basedir.endsWith("war"+ File.separatorChar) ? ".." + File.separatorChar : "")
                            + ".." + File.separatorChar + "war" + File.separatorChar;
                }
            }

        }catch (MalformedURLException ex) {
            throw new ResourceLoadException(ex.getMessage(), ex);
        }

    }

    private String getResourceDirectory() {

        return "classes" + File.separatorChar;
    }

    protected String getUrlFor(final String resource) {

        return resource;
    }

    @Override
    protected InputStream getInputStreamFor(URL url) {

        try {
            return new FileInputStream(new File(url.getFile()));
        } catch (IOException e) {
            throw new ResourceLoadException(readResourceDebug(url), e);
        }
    }

    private static class LocalTestEntityResolver implements EntityResolver {

        private static final Logger LOG = Logger.getLogger(LocalTestEntityResolver.class);
        private static final String INFO_LOADING_DTD = "Loading local DTD ";


        public InputSource resolveEntity(final String publicId, final String systemId) {

            // the latter is only for development purposes when dtds have't been published to production yet
            if (systemId.startsWith("http://sesam.no/dtds/") || systemId.startsWith("http://localhost")) {

                final String suffix = "war" + File.separatorChar
                    + "src" + File.separatorChar
                    + "main" + File.separatorChar
                    + "webapp" + File.separatorChar
                    + "dtds"
                    + systemId.substring(systemId.lastIndexOf('/'));

                String basedir = System.getProperty("basedir") + File.separatorChar;

                int genericSesamLoop = 0;

                while(true){

                    final String basedirNormalised = new File(basedir).toURI().normalize().toString()
                        .replaceFirst("file:", "")
                        .replace('/', File.separatorChar);

                    if("/".equals(basedirNormalised)){

                        throw new IllegalStateException("At root of filesystem! looking for " + suffix
                                + " . Current requirement of tests is that sesat-kernel is checked out, and named such,"
                                + " in any parent folder from here."
                                + "I've searched all the way to the root of the filesystem");
                    }

                    LOG.debug("looking in " + basedirNormalised + suffix);
                    final File f = new File(basedirNormalised + suffix);
                    if(f.exists()){
                        try{
                            return new InputSource(new FileInputStream(f));

                        }catch (FileNotFoundException ex) {
                            throw new ResourceLoadException(ex.getMessage(), ex);
                        }
                    }

                    ++genericSesamLoop;
                    basedir = System.getProperty("basedir") + File.separatorChar;
                    for(int i = 0; i < genericSesamLoop; ++i){
                        basedir = basedir + ".." + File.separatorChar;
                    }
                }

            } else {
                // use the default behaviour
                return null;
            }
        }

    }
}
