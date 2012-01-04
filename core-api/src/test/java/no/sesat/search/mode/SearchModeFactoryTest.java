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
 * SearchModeFactoryTest.java
 *
 * Created on April 19, 2006, 3:31 PM
 */

package no.sesat.search.mode;


import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import static org.testng.AssertJUnit.*;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.site.SiteTestCase;
import no.sesat.search.site.config.*;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 *
 *
 */
public final class SearchModeFactoryTest extends SiteTestCase {

    private static final Logger LOG = Logger.getLogger(SearchModeFactoryTest.class);

    /** TODO comment me. **/
    public SearchModeFactoryTest(final String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {

    }

    /** TODO comment me. **/
    protected void tearDown() throws Exception {
    }

    /**
     * Test of instanceOf method, of class no.sesat.search.configuration.SearchModeFactory.
     */
    @Test
    public void testGetModeFactory(){

        final Site.Context siteConstructorContext = getSiteConstructingContext();
        assertNotNull(getModeFactory(siteConstructorContext, null));
    }

    private SearchModeFactory getModeFactory(final Site.Context siteConstructorContext, final Locale locale) {

        LOG.trace("getModeFactory");

        final SearchModeFactory.Context cxt = new SearchModeFactory.Context(){
            public Site getSite()  {
                return locale == null
                        ? getTestingSite()
                        : Site.valueOf(siteConstructorContext, getTestingSite().getName(), locale);
            }
            public PropertiesLoader newPropertiesLoader(
                    final SiteContext siteCxt,
                    final String resource,
                    final Properties properties) {

                return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
            public DocumentLoader newDocumentLoader(
                    final SiteContext siteCxt,
                    final String resource,
                    final DocumentBuilder builder) {

                return FileResourceLoader.newDocumentLoader(siteCxt, resource, builder);
            }

            public BytecodeLoader newBytecodeLoader(SiteContext context, String className, String jar) {
                return FileResourceLoader.newBytecodeLoader(context, className, jar);
            }
        };

        final SearchModeFactory result = SearchModeFactory.instanceOf(cxt);
        assert null != result : "Mode cannot be null for mode " + cxt.getSite();

        return result;
    }

    /**
     * Test of getMode method, of class no.sesat.search.configuration.SearchModeFactory.
     */
    @Test
    public void testGetMode() {

        LOG.trace("testGetMode");

        final String id = "default-mode";
        final Site.Context siteConstructorContext = getSiteConstructingContext();
        final SearchModeFactory instance = getModeFactory(siteConstructorContext, null);

        final SearchMode result = instance.getMode(id);
        assert null != result : "Result cannot be null for mode " + id;
    }


    /**
     * Test of memory against getMode method,
     * of class no.sesat.search.configuration.SearchModeFactory.
     */
    @Test
    public void testGetModeOnAllAvailableLocales() {

        LOG.trace("testGetModeOnAllAvailableLocales");

        final String id = "default-mode";
        final Site.Context siteConstructorContext = getSiteConstructingContext();

        System.gc();
        final long initialTotal = Runtime.getRuntime().totalMemory();
        final long initialFree = Runtime.getRuntime().freeMemory();
        LOG.info("Number of Available locales " + Locale.getAvailableLocales().length);

        for(Locale l : Locale.getAvailableLocales()){

            final Site site = Site.valueOf(siteConstructorContext, getTestingSite().getName(), l);
            final SiteConfiguration.Context siteConfCxt = new SiteConfiguration.Context(){
                public PropertiesLoader newPropertiesLoader(
                        final SiteContext siteCxt,
                        final String resource,
                        final Properties properties) {

                    return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
                }
                public Site getSite() {
                    return site;
                }
            };
            final SiteConfiguration siteConf = SiteConfiguration.instanceOf(siteConfCxt);

            if( siteConf.isSiteLocaleSupported(l) ){
                final SearchModeFactory instance = getModeFactory(siteConstructorContext, l);

                final SearchMode result = instance.getMode(id);
                assert null != result : "Result cannot be null for mode " + id;
            }
        }
        LOG.info("Total memory increased "+(Runtime.getRuntime().totalMemory()-initialTotal) + " bytes");
        LOG.info("Free memory decreased "+(initialFree-Runtime.getRuntime().freeMemory()) + " bytes");
        System.gc();
    }


    /**
     * Test of deserialisation of all modes that exist in this site's modes.xml.
     */
    @Test
    public void testAllModes() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        LOG.trace("testAllModes");

        final Site.Context siteConstructorContext = getSiteConstructingContext();
        final SearchModeFactory instance = getModeFactory(siteConstructorContext, null);

        final Map<String,SearchMode> modes = instance.getModes();

        for( String id : modes.keySet()){
            final SearchMode result = instance.getMode(id);
            assert null != result : "Result cannot be null for mode " + id;
        }
    }

}
