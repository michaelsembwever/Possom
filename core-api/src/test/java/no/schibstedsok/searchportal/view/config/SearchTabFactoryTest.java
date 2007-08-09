/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
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

import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.site.config.*;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/** Tests for SearchTabFactory. Using default search-config's configuration files.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class SearchTabFactoryTest extends SiteTestCase {

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
    @Test
    public void testGetViewFactory() {

        final Site.Context siteConstructorContext = getSiteConstructingContext();
        assertNotNull(getViewFactory(siteConstructorContext, null));
    }

    /**
     * Test of testGetTabByName method, of class no.schibstedsok.searchportal.view.config.SearchTabFactory.
     */
    @Test
    public void testGetTabByName() {

        LOG.trace("testGetTab");

        final String id = "local-internet";
        final Site.Context siteConstructorContext = getSiteConstructingContext();
        final SearchTabFactory instance = getViewFactory(siteConstructorContext, null);

        final SearchTab result = instance.getTabByName(id);
        assertNotNull(result);
    }

    /**
     * Test of testGetTabByKey method, of class no.schibstedsok.searchportal.view.config.SearchTabFactory.
     */
    @Test
    public void testGetTabByKey() {

        LOG.trace("testGetTab");

        final String key = "d";
        final Site.Context siteConstructorContext = getSiteConstructingContext();
        final SearchTabFactory instance = getViewFactory(siteConstructorContext, null);

        final SearchTab result = instance.getTabByKey(key);
        assertNotNull(result);
    }

    /**
     * Test of memory against getTabByKey method,
     * of class no.schibstedsok.searchportal.configuration.SearchModeFactory.
     */
    @Test
    public void testGetTabByKeyModeOnAllAvailableLocales() {
        LOG.trace("testGetModeOnAllAvailableLocales");

        final String key = "d";
        final Site.Context siteConstructorContext = getSiteConstructingContext();
        
        System.gc();
        final long initialTotal = Runtime.getRuntime().totalMemory();
        final long initialFree = Runtime.getRuntime().freeMemory();
        LOG.info("Number of Available locales " + Locale.getAvailableLocales().length);
        
        for(Locale l : Locale.getAvailableLocales()){
            
            
            final Site site = Site.valueOf(siteConstructorContext, getTestingSite().getName(), l);
            final SiteConfiguration.Context siteConfCxt = new SiteConfiguration.Context(){// <editor-fold defaultstate="collapsed" desc=" genericCxt ">
                public PropertiesLoader newPropertiesLoader(
                        final SiteContext siteCxt, 
                        final String resource, 
                        final Properties properties) {

                    return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
                }
                public Site getSite() {
                    return site;
                }
            };//</editor-fold>
            final SiteConfiguration siteConf = SiteConfiguration.valueOf(siteConfCxt);
            
            if( siteConf.isSiteLocaleSupported(l) ){
                final SearchTabFactory instance = getViewFactory(siteConstructorContext, l);

                final SearchTab result = instance.getTabByKey(key);
                assertNotNull(result);
            }
        }
        LOG.info("Total memory increased "+(Runtime.getRuntime().totalMemory()-initialTotal) + " bytes");
        LOG.info("Free memory decreased "+(initialFree-Runtime.getRuntime().freeMemory()) + " bytes");
        System.gc();
    }


    private SearchTabFactory getViewFactory(final Site.Context siteConstructorContext, final Locale locale) {

        LOG.trace("getModeFactory");

        final SearchTabFactory.Context cxt = new SearchTabFactory.Context(){
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
            public Site getSite()  {
                return locale == null
                        ? getTestingSite()
                        : Site.valueOf(siteConstructorContext, getTestingSite().getName(), locale);
            }
            public BytecodeLoader newBytecodeLoader(SiteContext context, String className, String jar) {
                return FileResourceLoader.newBytecodeLoader(context, className, jar);
            }

        };

        final SearchTabFactory result = SearchTabFactory.valueOf(cxt);
        assertNotNull(result);

        return result;
    }

}
