// Copyright (2007) Schibsted Søk AS
/*
 * VelocityTemplateTest.java
 *
 * Created on 1 March 2007, 09:35
 *
 */

package no.schibstedsok.searchportal.view.velocity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.DataModelTestCase;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.ResourceLoadException;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.testng.annotations.Test;

/** Tests that all the velocity templates in the current skin are syntactically.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class VelocityTemplateTest extends DataModelTestCase{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(VelocityTemplateTest.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of VelocityTemplateTest */
    public VelocityTemplateTest() {

        // can only be done in a testing JVM
        URLVelocityTemplateLoader.setContext(new VelocityLoader());
    }

    // Public --------------------------------------------------------

    @Test
    public void parseAllTemplates() {

        final StringBuilder errors = new StringBuilder();

        final String base = System.getProperty("basedir") + "/src/main/templates/";
        LOG.info("Looking in " + base);

        final File templatesFolder = new File(base);
        final List<File> templates = new ArrayList<File>();
        collectTemplates(templatesFolder, templates);

        for(File file : templates){
            LOG.info("Testing merge against " + file.getAbsolutePath());
            try{
                final String templateName = file.getAbsolutePath().replaceFirst(base, "");
                final DataModel datamodel = getDataModel();
                final Site site = datamodel.getSite().getSite();
                final VelocityEngine engine = VelocityEngineFactory.valueOf(site).getEngine();
                final Template template = VelocityEngineFactory.getTemplate(engine, site, templateName);
                final VelocityContext context = VelocityEngineFactory.newContextInstance(engine);
                context.put("datamodel", datamodel);
                template.merge(context, new StringWriter());

            }catch(MethodInvocationException mie){
                LOG.debug(file.getAbsolutePath().replaceFirst(base, "") + " ignoring " + mie.getMessage());
            }catch(IOException ioe){
                LOG.debug(file.getAbsolutePath().replaceFirst(base, "") + " ignoring " + ioe.getMessage());

            }catch(Exception e){
                LOG.error(e.getMessage(), e);
                errors.append(file.getAbsolutePath() + " failed with " + e.getMessage() + "\n--\n");
            }
        }

        assert 0 == errors.length() : "\n--\n" + errors.toString();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void collectTemplates(final File directory, final List<File> templates){

        final File[] arr = directory.listFiles(new FilenameFilter(){
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".vm");
            }
        });

        if( null != arr && 0 < arr.length ){
            templates.addAll(Arrays.asList(arr));
        }

        for( File file : directory.listFiles()){
            if(file.isDirectory()){
                collectTemplates(file, templates);
            }
        }
    }

    // Inner classes -------------------------------------------------

    private final class VelocityLoader extends FileResourceLoader implements URLVelocityTemplateLoader.Context{

        VelocityLoader(){
            super(new SiteContext(){
                public Site getSite() {
                    //return getTestingSite(); <-- doesn't work because of java bug #6266772
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6266772
                    return VelocityTemplateTest.this.getTestingSite();
                }
            });
        }

        public boolean doesUrlExist(final URL url) {
            return urlExists(url);
        }

        public URL getURL(final String resource, final Site site) {
            LOG.trace("getURL(" + resource + ')');

            try{

                final String siteFolder = site.getConfigContext();

                final String base = System.getProperty("basedir") // test jvm sets this property
                        + (System.getProperty("basedir").endsWith("war") ? "/../../" : "/../")
                        + getProjectName(siteFolder);

                final File wf = new File(base + "/war");

                final String rsc = resource
                        .substring(resource.lastIndexOf("templates/") + 10)
                        .replaceAll(".vm.vm", ".vm");

                return new URI("file://"
                        + base
                        + (wf.exists() && wf.isDirectory() ? "/war/src/main/templates/" : "/src/main/templates/")
                        + rsc).normalize().toURL();

            }catch (URISyntaxException ex) {
                throw new ResourceLoadException(ex.getMessage(), ex);
            } catch (final MalformedURLException ex) {
                throw new ResourceLoadException(ex.getMessage(), ex);
            }
        }
    }
}
