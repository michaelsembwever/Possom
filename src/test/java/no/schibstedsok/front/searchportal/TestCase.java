/* Copyright (2005-2006) Schibsted Søk AS
 *
 * TestCase.java
 *
 * Created on June 28, 2006, 12:06 PM
 *
 */

package no.schibstedsok.front.searchportal;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class TestCase extends junit.framework.TestCase {
    
    // Constants -----------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(TestCase.class);
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    static{
        // This will be dangerous if this class is loaded in non-test operation
        if(Logger.getRootLogger().getAppender("TEST_APPEND") == null){
            // switch appenders over to the test logfile
            Logger.getRootLogger().removeAllAppenders();
            Logger.getRootLogger().addAppender(LOG.getAppender("TEST_APPEND"));
        }
    }
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of TestCase */
    public TestCase(final String testName) {
        super(testName);
    }
    
    // Public --------------------------------------------------------
    
    // Z implementation ----------------------------------------------
    
    // no.schibstedsok.front.searchportal.TestCase overrides ----------------------------
    
    protected void setUp() throws Exception {
        super.setUp();

        
        MDC.put("test", getClass().getSimpleName());
        
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        
        MDC.remove("test");
    }    
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------


    
    
    
}
