/*
 * NormalizePhoneNumberTransformerTest.java
 * JUnit based test
 *
 * Created on February 10, 2006, 9:27 AM
 */

package no.schibstedsok.front.searchportal.query;

import junit.framework.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author magnuse
 */
public class NormalizePhoneNumberTransformerTest extends TestCase {
    
    public NormalizePhoneNumberTransformerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(NormalizePhoneNumberTransformerTest.class);
        
        return suite;
    }

    /**
     * Test of getTransformedQuery method, of class no.schibstedsok.front.searchportal.query.NormalizePhoneNumberTransformer.
     */
    public void testGetTransformedQuery() {
        // https://jira.sesam.no/jira/browse/CAT-328
        String originalQuery = "37020047";
        NormalizePhoneNumberTransformer instance = new NormalizePhoneNumberTransformer();
        String result = instance.getTransformedQuery(originalQuery);
        assertEquals(originalQuery, result);
    }
}
