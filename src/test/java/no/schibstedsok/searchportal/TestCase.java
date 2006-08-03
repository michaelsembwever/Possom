/* Copyright (2005-2006) Schibsted Søk AS
 *
 * TestCase.java
 *
 * Created on June 28, 2006, 12:06 PM
 *
 */

package no.schibstedsok.searchportal;

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
    
        removingAppendersWarning();

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

    // no.schibstedsok.searchportal.TestCase overrides ----------------------------

    /** TODO comment me. **/
    protected void setUp() throws Exception {
        super.setUp();

        
        MDC.put("test", getClass().getSimpleName());
        
    }

    /** TODO comment me. **/
    protected void tearDown() throws Exception {
        super.tearDown();

        MDC.remove("test");
    }    

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    
    private static void removingAppendersWarning(){
        
        LOG.warn("==================================");
        LOG.warn("REMOVING ALL APPENDERS FOR TESTING");
        LOG.warn("==================================");
        
        System.out.println("==================================");
        System.out.println("REMOVING ALL APPENDERS FOR TESTING");
        System.out.println("==================================");
    }
    
    // Inner classes -------------------------------------------------



    
    
}
