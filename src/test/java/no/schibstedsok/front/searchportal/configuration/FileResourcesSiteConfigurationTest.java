// Copyright (2006) Schibsted Søk AS
/*
 * FileResourcesSiteConfigurationTest.java
 * JUnit based test
 *
 * Created on 22 January 2006, 16:05
 */

package no.schibstedsok.front.searchportal.configuration;

import java.util.Properties;
import no.schibstedsok.front.searchportal.TestCase;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.site.Site;

/**
 * Tests using SearchTabsCreator against File-based configuration files.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class FileResourcesSiteConfigurationTest extends TestCase {

    private static final String FAIL_CONFIG_NOT_FOUND =
            "\n\n"
            + "Unable to obtain configuration resources from classloader. \n"
            + "\n";
	    
    public FileResourcesSiteConfigurationTest(final String testName) {
        super(testName);
    }	    

    /**
     * Test of valueOf method, of class no.schibstedsok.front.searchportal.configuration.SiteConfiguration.
     */
    public void testDefaultSite() {

        final Site site = Site.DEFAULT;

        final SiteConfiguration result = valueOf(site);
        assertNotNull(FAIL_CONFIG_NOT_FOUND, result);
    }


    /**
     * Test of getProperties method, of class no.schibstedsok.front.searchportal.configuration.SiteConfiguration.
     */
    public void testDefaultSiteGetProperties() {

        final SiteConfiguration instance = valueOf(Site.DEFAULT);

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
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }

        });
        return stc;
    }
}
