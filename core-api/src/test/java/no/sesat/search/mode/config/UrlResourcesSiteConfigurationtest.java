/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 * FileResourcesSearchTabsCreatorTest.java
 *
 * Created on 22 January 2006, 16:05
 */

package no.sesat.search.mode.config;


import java.util.Properties;
import no.sesat.search.site.SiteTestCase;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.site.Site;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/**
 * Tests using SearchTabsCreator against URL-based configuration files.
 * Only to be run when an application server is up and running.
 * 
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: UrlResourcesSiteConfigurationtest.java 3359 2006-08-03 08:13:22Z mickw $
 */
@Test(groups = {"requires-tomcat"})
public final class UrlResourcesSiteConfigurationtest extends SiteTestCase {

    private static final String FAIL_CONFIG_NOT_RUNNING =
            "\n\n"
            + "Unable to obtain configuration resources from search-front-config. \n"
            + "Please start this service before trying to build/deploy search-front-html."
            + "\n";

    public UrlResourcesSiteConfigurationtest(final String testName) {
        super(testName);
    }	     
    
    /**
     * Test of valueOf method, of class no.sesat.search.configuration.SiteConfiguration.
     */
    @Test
    public void testDefaultSite() {

        final Site site = Site.DEFAULT;

        final SiteConfiguration result = SiteConfiguration.valueOf(site);
        assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
    }

    /**
     * Test of getProperties method, of class no.sesat.search.configuration.SiteConfiguration.
     */
    @Test
    public void testDefaultSiteGetProperties() {

        final SiteConfiguration instance = SiteConfiguration.valueOf(Site.DEFAULT);

        final Properties result = instance.getProperties();
        assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
    }

}
