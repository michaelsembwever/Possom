/* Copyright (2006-2007) Schibsted Søk AS
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

 */
/*
 * FileResourcesSiteConfigurationTest.java
 *
 * Created on 22 January 2006, 16:05
 */

package no.sesat.search.site.config;

import java.util.Properties;
import no.sesat.search.site.SiteTestCase;
import static org.testng.AssertJUnit.*;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
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
     * Test of valueOf method, of class no.sesat.search.configuration.SiteConfiguration.
     */
    @Test
    public void testDefaultSite() {

        final Site site = Site.DEFAULT;

        final SiteConfiguration result = valueOf(site);
        assertNotNull(FAIL_CONFIG_NOT_FOUND, result);
    }


    /**
     * Test of getProperties method, of class no.sesat.search.configuration.SiteConfiguration.
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
