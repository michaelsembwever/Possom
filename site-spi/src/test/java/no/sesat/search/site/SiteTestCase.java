/* Copyright (2005-2007) Schibsted Søk AS
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
 *
 * TestCase.java
 *
 * Created on June 28, 2006, 12:06 PM
 *
 */

package no.sesat.search.site;

import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import no.sesat.search.site.Site;
import no.sesat.search.site.Site.Context;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class SiteTestCase {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(SiteTestCase.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    static{
    
//        removingAppendersWarning();
//
//        // This will be dangerous if this class is loaded in non-test operation
//        if(Logger.getRootLogger().getAppender("TEST_APPEND") == null){
//            // switch appenders over to the test logfile
//            Logger.getRootLogger().removeAllAppenders();
//            Logger.getRootLogger().addAppender(LOG.getAppender("TEST_APPEND"));
//        }
    }

    // Constructors --------------------------------------------------
    
    protected SiteTestCase(){}

    /** Creates a new instance of TestCase */
    public SiteTestCase(final String testName) {
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // no.sesat.search.TestCase overrides ----------------------------

    @BeforeClass
    protected void setUp() throws Exception {
        MDC.put("test", getClass().getSimpleName());
        
    }

    @AfterClass
    protected void tearDown() throws Exception {
        MDC.remove("test");
    }    

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------
    
    protected Site.Context getSiteConstructingContext(){
        
        return new Context(){
            public String getParentSiteName(final SiteContext siteContext){
                // we have to do this manually instead of using SiteConfiguration,
                //  because SiteConfiguration relies on the parent site that we haven't get initialised.
                // That is, the PARENT_SITE_KEY property MUST be explicit in the site's configuration.properties.
                final Properties props = new Properties();
                final PropertiesLoader loader
                        = FileResourceLoader.newPropertiesLoader(siteContext, Site.CONFIGURATION_FILE, props);
                loader.abut();
                return props.getProperty(Site.PARENT_SITE_KEY);
            }
        };
    }
    
    protected final Site getTestingSite(){
        
        final String basedir = System.getProperty("basedir").replaceAll(Matcher.quoteReplacement(File.separatorChar + "war"), "");
        final Site result = Site.valueOf(
                getSiteConstructingContext(),
                basedir.substring(basedir.lastIndexOf(File.separatorChar)+1).replaceAll(Matcher.quoteReplacement(File.separator), ""),
                Locale.getDefault());
        
        if(LOG.isDebugEnabled()){
            final StringBuilder sb = new StringBuilder("Writing out site ancestory\n");
            for(Site s = result;; s = s.getParent()){
                sb.append(s.toString());
                if(null != s.getParent()){ 
                    sb.append(" --> "); 
                }else{
                    break;
                }
            }
            LOG.debug(sb);
        }
        
        return result;
    }    

    // Private -------------------------------------------------------
    
    private static void removingAppendersWarning(){
        
        LOG.warn("==================================");
        LOG.warn("REMOVING ALL APPENDERS FOR TESTING");
        LOG.warn("==================================");
        
        System.out.println("\n==================================");
        System.out.println("REMOVING ALL APPENDERS FOR TESTING");
        System.out.println("==================================");
    }
    
    // Inner classes -------------------------------------------------



    
    
}
