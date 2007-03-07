// Copyright (2006-2007) Schibsted Søk AS
/*
 * SearchModeFactoryTest.java
 *
 * Created on April 19, 2006, 3:31 PM
 */

package no.schibstedsok.searchportal.mode;


import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import static org.testng.AssertJUnit.*;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
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
     * Test of valueOf method, of class no.schibstedsok.searchportal.configuration.SearchModeFactory.
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
        };

        final SearchModeFactory result = SearchModeFactory.valueOf(cxt);
        assert null != result : "Mode cannot be null for mode " + cxt.getSite();

        return result;
    }

    /**
     * Test of getMode method, of class no.schibstedsok.searchportal.configuration.SearchModeFactory.
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
     * of class no.schibstedsok.searchportal.configuration.SearchModeFactory.
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
            final SiteConfiguration siteConf = SiteConfiguration.valueOf(siteConfCxt);
            
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
