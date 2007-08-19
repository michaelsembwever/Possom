/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * FactoryReloadsTest.java
 *
 * Created on 5 May 2006, 07:50
 */

package no.sesat.searchportal.http.servlet;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.searchportal.http.servlet.FactoryReloads.ReloadArg;
import no.sesat.searchportal.site.SiteTestCase;
import no.sesat.searchportal.site.config.DocumentLoader;
import no.sesat.searchportal.site.config.FileResourceLoader;
import no.sesat.searchportal.site.config.PropertiesLoader;
import no.sesat.searchportal.site.config.BytecodeLoader;
import no.sesat.searchportal.site.*;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;


/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class FactoryReloadsTest extends SiteTestCase {
    
    private static final Logger LOG = Logger.getLogger(FactoryReloadsTest.class);
    
    public FactoryReloadsTest(String testName) {
        super(testName);
    }

    /**
     * Test of remove method, of class no.sesat.searchportal.servlet.FactoryReloads.
     */
    @Test
    public void testRemoveAll() {
        
        FactoryReloads.performReloads(genericContext(), ReloadArg.SITE_CONFIGURATION); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.SEARCH_TAB_FACTORY); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.SEARCH_MODE_FACTORY); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.ANALYSIS_RULES_FACTORY); 
        FactoryReloads.performReloads(genericContext(), ReloadArg.REG_EXP_EVALUATOR_FACTORY); 
        // skip "velocity" because VelocityEngineFactory harcodes to URLVelocityTemplateLoader
        
    }

    private SiteContext genericContext(){
        
        // BaseContext providing SiteContext and ResourceContext.
        //  We need it casted as a SiteContext for the ResourceContext code to be happy.
        return new SiteContext(){
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
            
            public Site getSite() {
                return getTestingSite();
            }
        };
    }
}
