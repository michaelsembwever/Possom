// Copyright (2006) Schibsted Søk AS
/*
 * SearchTabFactoryTest.java
 * JUnit based test
 *
 * Created on 20 April 2006, 11:30
 */

package no.schibstedsok.searchportal.view.config;

import java.util.Locale;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.TestCase;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.util.config.DocumentLoader;
import no.schibstedsok.searchportal.util.config.FileResourceLoader;
import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.Site;
import org.apache.log4j.Logger;

/** Tests for SearchTabFactory. Using default search-config's configuration files.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class SearchTabFactoryTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(SearchTabFactoryTest.class);

    /** TODO comment me. **/
    public SearchTabFactoryTest(final String testName) {
        super(testName);
    }

    /** TODO comment me. **/
    protected void setUp() throws Exception {
    }

    /** TODO comment me. **/
    protected void tearDown() throws Exception {
    }

    /**
     * Test of getViewFactory method, of class no.schibstedsok.searchportal.view.config.SearchTabFactory.
     */
    public void testGetViewFactory() {

        assertNotNull(getViewFactory(null));
    }

    /**
     * Test of testGetTabByName method, of class no.schibstedsok.searchportal.view.config.SearchTabFactory.
     */
    public void testGetTabByName() {

        LOG.trace("testGetTab");

        final String id = "local-internet";
        final SearchTabFactory instance = getViewFactory(null);

        final SearchTab result = instance.getTabByName(id);
        assertNotNull(result);
    }

    /**
     * Test of testGetTabByKey method, of class no.schibstedsok.searchportal.view.config.SearchTabFactory.
     */
    public void testGetTabByKey() {

        LOG.trace("testGetTab");

        final String key = "d";
        final SearchTabFactory instance = getViewFactory(null);

        final SearchTab result = instance.getTabByKey(key);
        assertNotNull(result);
    }

    /**
     * Test of memory against getTabByKey method,
     * of class no.schibstedsok.searchportal.configuration.SearchModeFactory.
     */
    public void testGetTabByKeyModeOnAllAvailableLocales() {
        LOG.trace("testGetModeOnAllAvailableLocales");

        final String key = "d";
        System.gc();
        final long initialTotal = Runtime.getRuntime().totalMemory();
        final long initialFree = Runtime.getRuntime().freeMemory();
        LOG.info("Number of Available locales " + Locale.getAvailableLocales().length);
        for(Locale l : Locale.getAvailableLocales()){
            final SearchTabFactory instance = getViewFactory(l);

            final SearchTab result = instance.getTabByKey(key);
            assertNotNull(result);
        }
        LOG.info("Total memory increased "+(Runtime.getRuntime().totalMemory()-initialTotal) + " bytes");
        LOG.info("Free memory decreased "+(initialFree-Runtime.getRuntime().freeMemory()) + " bytes");
        System.gc();
    }


    private SearchTabFactory getViewFactory(final Locale locale) {

        LOG.trace("getModeFactory");

        final SearchTabFactory.Context cxt = new SearchTabFactory.Context(){

            private final Site.Context siteConstructorContext = new Site.Context(){
                public String getParentSiteName(final SiteContext siteContext){
                    return Site.DEFAULT.getName();
                }
            };

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            public Site getSite()  {
                return locale == null
                        ? Site.DEFAULT
                        : Site.valueOf(siteConstructorContext, Site.DEFAULT.getName(), locale);
            }
        };

        final SearchTabFactory result = SearchTabFactory.valueOf(cxt);
        assertNotNull(result);

        return result;
    }

}
