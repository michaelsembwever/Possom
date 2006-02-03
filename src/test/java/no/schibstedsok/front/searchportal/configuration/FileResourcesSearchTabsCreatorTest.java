// Copyright (2006) Schibsted Søk AS
/*
 * FileResourcesSearchTabsCreatorTest.java
 * JUnit based test
 *
 * Created on 22 January 2006, 16:05
 */

package no.schibstedsok.front.searchportal.configuration;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.site.Site;

/** Tests using SearchTabsCreator against File-based configuration files.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class FileResourcesSearchTabsCreatorTest extends TestCase {

    private static final String FAIL_CONFIG_NOT_FOUND =
            "\n\n"
            + "Unable to obtain configuration resources from classloader. \n"
            + "\n";


    /**
     * Test of valueOf method, of class no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator.
     */
    public void testDefaultSite() {

        final Site site = Site.DEFAULT;

        final SearchTabsCreator result = valueOf(site);
        assertNotNull(FAIL_CONFIG_NOT_FOUND, result);
    }

    /**
     * Test of getSearchTabs method, of class no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator.
     */
    public void testDefaultSiteGetSearchTabs() {

        final SearchTabsCreator instance = valueOf(Site.DEFAULT);

        final SearchTabs result = instance.getSearchTabs();
        assertNotNull(FAIL_CONFIG_NOT_FOUND, result);
    }

    /**
     * Test of getProperties method, of class no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator.
     */
    public void testDefaultSiteGetProperties() {

        final SearchTabsCreator instance = valueOf(Site.DEFAULT);

        final Properties result = instance.getProperties();
        assertNotNull(FAIL_CONFIG_NOT_FOUND, result);
    }


    /** Utility wrapper to the valueOf(Context).
     * <b>Makes the presumption we will be using the FileResourceLoader to load all resources.</b>
     *This is useful for tests that don't wont to rely on an application server being up to serve the config files.
     * @param site the site to obtain a SearchTabsCreator for
     * @return the searchTabsCreator to use.
     **/
    public static SearchTabsCreator valueOf(final Site site) {

        // XMLSearchTabsCreator.Context for this site & UrlResourceLoader.
        final SearchTabsCreator stc = XMLSearchTabsCreator.valueOf(new XMLSearchTabsCreator.Context() {
            public Site getSite()  {
                return site;
            }

            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return FileResourceLoader.newXStreamLoader(this, resource, xstream);
            }
            
            public DocumentLoader newDocumentLoader(String resource, DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }

        });
        return stc;
    }
}
