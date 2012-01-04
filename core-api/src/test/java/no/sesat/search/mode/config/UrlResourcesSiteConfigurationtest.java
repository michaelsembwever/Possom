/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

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
 *
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
     * Test of instanceOf method, of class no.sesat.search.configuration.SiteConfiguration.
     */
    @Test
    public void testDefaultSite() {

        final Site site = Site.DEFAULT;

        final SiteConfiguration result = SiteConfiguration.instanceOf(site);
        assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
    }

    /**
     * Test of getProperties method, of class no.sesat.search.configuration.SiteConfiguration.
     */
    @Test
    public void testDefaultSiteGetProperties() {

        final SiteConfiguration instance = SiteConfiguration.instanceOf(Site.DEFAULT);

        final Properties result = instance.getProperties();
        assertNotNull(FAIL_CONFIG_NOT_RUNNING, result);
    }

}
