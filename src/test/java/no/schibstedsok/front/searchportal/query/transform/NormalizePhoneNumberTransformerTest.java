// Copyright (2006) Schibsted Søk AS
/*
 * NormalizePhoneNumberTransformerTest.java
 * JUnit based test
 *
 * Created on February 10, 2006, 9:27 AM
 */

package no.schibstedsok.front.searchportal.query.transform;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.site.Site;
/**
 *
 * @author magnuse
 */
public final class NormalizePhoneNumberTransformerTest extends TestCase {
    public NormalizePhoneNumberTransformerTest(final String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite(NormalizePhoneNumberTransformerTest.class);

        return suite;
    }

    /**
     * Test of getTransformedQuery method, of class no.schibstedsok.front.searchportal.query.NormalizePhoneNumberTransformer.
     */
    public void testGetTransformedQuery() {
        // https://jira.sesam.no/jira/browse/CAT-328
        final String originalQuery = "37020047";
        final NormalizePhoneNumberTransformer instance = new NormalizePhoneNumberTransformer();
        instance.setContext(createCtx(originalQuery));
        final String result = instance.getTransformedQuery();
        assertEquals(originalQuery, result);
    }

    private QueryTransformer.Context createCtx(final String originalQuery) {
        return createCtx(originalQuery, Site.DEFAULT);
    }

    private QueryTransformer.Context createCtx(final String originalQuery, final Site site) {
        final QueryTransformer.Context ctx = new QueryTransformer.Context() {

            public Site getSite() {
                return site;
            }

            public String getTransformedQuery() {
                return originalQuery;
            }

            public String getQueryString() {
                return originalQuery;
            }

            public Map/*<Clause,String>*/ getTransformedTerms() {
                return null;
            }

            public Query getQuery() {
                return null;
            }

        };
        return ctx;
    }
}
