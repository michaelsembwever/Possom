/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 * FileResourcesSiteConfigurationTest.java
 *
 * Created on 22 January 2006, 16:05
 */

package no.sesat.searchportal.site.config;

import java.util.Properties;
import no.sesat.searchportal.site.SiteTestCase;
import static org.testng.AssertJUnit.*;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.SiteContext;
import org.testng.annotations.Test;

/**
 * Tests using SearchTabsCreator against File-based configuration files.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: FileResourcesSiteConfigurationTest.java 3359 2006-08-03 08:13:22Z mickw $
 */
public final class FileResourcesSiteConfigurationTest extends SiteTestCase {

    private static final String FAIL_CONFIG_NOT_FOUND =
            "\n\n"
            + "Unable to obtain configuration resources from classloader. \n"
            + "\n";
	    
    public FileResourcesSiteConfigurationTest(final String testName) {
        super(testName);
    }	    

    /**
     * Test of valueOf method, of class no.sesat.searchportal.configuration.SiteConfiguration.
     */
    @Test
    public void testDefaultSite() {

        final Site site = Site.DEFAULT;

        final SiteConfiguration result = valueOf(site);
        assertNotNull(FAIL_CONFIG_NOT_FOUND, result);
    }


    /**
     * Test of getProperties method, of class no.sesat.searchportal.configuration.SiteConfiguration.
     */
    @Test
    public void testDefaultSiteGetProperties() {

        final SiteConfiguration instance = valueOf(getTestingSite());

        final Properties result = instance.getProperties();
        assertNotNull(FAIL_CONFIG_NOT_FOUND, result);
    }


    /** Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the FileResourceLoader to load all resources.</b>
     *This is useful for tests that don't wont to rely on an application server being up to serve the config files.
     * @param site the site to obtain a SearchTabsCreator for
     * @return the searchTabsCreator to use.
     **/
    public static SiteConfiguration valueOf(final Site site) {

        // SiteConfiguration.Context for this site & UrlResourceLoader.
        final SiteConfiguration stc = SiteConfiguration.valueOf(new SiteConfiguration.Context() {
            public Site getSite()  {
                return site;
            }
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt, 
                    final String resource, 
                    final Properties properties) {
                
                return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
        });
        return stc;
    }
}
