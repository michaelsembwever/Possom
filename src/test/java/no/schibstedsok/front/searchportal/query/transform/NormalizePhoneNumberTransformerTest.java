/*
 * NormalizePhoneNumberTransformerTest.java
 * JUnit based test
 *
 * Created on February 10, 2006, 9:27 AM
 */

package no.schibstedsok.front.searchportal.query.transform;

import junit.framework.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.schibstedsok.front.searchportal.site.Site;
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
        final String originalQuery = "37020047";
        final NormalizePhoneNumberTransformer instance = new NormalizePhoneNumberTransformer();
        final String result = instance.getTransformedQuery(createCtx(originalQuery));
        assertEquals(originalQuery, result);
    }
    
    private QueryTransformer.Context createCtx(final String originalQuery) {
        final QueryTransformer.Context ctx = new QueryTransformer.Context() {
            public Site getSite() {
                return Site.DEFAULT;
            }
            
            public String getQueryString() {
                return originalQuery;
            }
            
        };
        return ctx;
    }
    
    private QueryTransformer.Context createCtx(final String originalQuery, final Site site) {
        final QueryTransformer.Context ctx = new QueryTransformer.Context() {
            public Site getSite() {
                return site;
            }
            
            public String getQueryString() {
                return originalQuery;
            }
            
        };
        return ctx;
    }
}
