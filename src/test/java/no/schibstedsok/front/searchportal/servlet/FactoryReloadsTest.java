/*
 * FactoryReloadsTest.java
 * JUnit based test
 *
 * Created on 5 May 2006, 07:50
 */

package no.schibstedsok.front.searchportal.servlet;

import java.util.Locale;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.SearchModeFactory;
import no.schibstedsok.front.searchportal.configuration.SiteConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.query.analyser.AnalysisRuleFactory;
import no.schibstedsok.front.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.front.searchportal.site.*;
import no.schibstedsok.front.searchportal.view.config.SearchTabFactory;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class FactoryReloadsTest extends TestCase {
    
    private static final Logger LOG = Logger.getLogger(FactoryReloadsTest.class);
    
    public FactoryReloadsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of remove method, of class no.schibstedsok.front.searchportal.servlet.FactoryReloads.
     */
    public void testRemoveAll() {
        
        FactoryReloads.performReloads(genericContext(), "all"); 
        
    }

    private SiteContext genericContext(){
        
        // BaseContext providing SiteContext and ResourceContext.
        //  We need it casted as a SiteContext for the ResourceContext code to be happy.
        return new SiteContext(){
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            public Site getSite() {
                return Site.DEFAULT;
            }
        };
    }
}
