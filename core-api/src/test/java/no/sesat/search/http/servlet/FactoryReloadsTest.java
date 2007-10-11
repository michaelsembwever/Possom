/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * FactoryReloadsTest.java
 *
 * Created on 5 May 2006, 07:50
 */

package no.sesat.search.http.servlet;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.http.servlet.FactoryReloads.ReloadArg;
import no.sesat.search.site.SiteTestCase;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.*;
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
     * Test of remove method, of class no.sesat.search.servlet.FactoryReloads.
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
