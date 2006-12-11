package no.schibstedsok.searchportal.mode.command;


import java.util.Hashtable;

/**
 *
 */
public class YahooMediaSearchCommandTest extends AbstractSearchCommandTest {
    /**
     * Creates a new instance of YahooMediaSearchCommandTest
     *
     * @param testName The name of the test.
     */
    public YahooMediaSearchCommandTest(final String testName) {
        super(testName);
    }

    /**
     * Test a single term.
     */
    public void testSingleTerm() {
        executeTestOfQuery("test", "test", "");
    }


    public void testTwoTerms() {
        executeTestOfQuery("test1 test2", "test1 AND test2", "");
    }

    public void testThreeTerms() {
        executeTestOfQuery("test1 test2 test3", "test3 AND test1 AND test2", "");
    }

    public void testTwoTermsPlus() {
        executeTestOfQuery("+test1 +test2", "test1 AND test2", "");
    }


    public void testNot() {
        executeTestOfQuery("test1 -test2", "test1 ANDNOT test2", "");
    }

    public void testLeadingNot() {
        executeTestOfQuery("-test1 test2", "test2 ANDNOT test1", "");
    }

    public void testOr() {
        executeTestOfQuery("(test1 test2)", "(test1 OR test2)", "");
    }

    public void testLeadingNotAndOr() {
        executeTestOfQuery("-a (test1 test2)", "(test1 OR test2) ANDNOT a", "");
    }

    public void testLotsOfNots() {
        executeTestOfQuery("-a d -b -e c -f", "c AND d ANDNOT a ANDNOT b ANDNOT e ANDNOT f", "");
        executeTestOfQuery("-a d -b -e c g -f", "g AND c AND d ANDNOT a ANDNOT b ANDNOT e ANDNOT f", "");
        executeTestOfQuery("-a -b c", "c ANDNOT a ANDNOT b", "");
        executeTestOfQuery("-a (e f) c", "c AND (e OR f) ANDNOT a", "");
    }


    public void testSiteRestriction() {
        executeTestOfQuery("site:aftonbladet.se banan", "banan", "");
        executeTestOfQuery("banan site:aftonbladet.se", "banan", "");
        
        final SearchCommand.Context cxt = createCommandContext("site:aftonbladet.se banan", "d", "yahoo-image-search");
        final AbstractYahooSearchCommand cmd = new YahooMediaSearchCommand(cxt, new Hashtable<String,Object>());
        cmd.getQueryRepresentation(cxt.getQuery());
        assertTrue(cmd.createRequestURL().contains("rurl=http://aftonbladet.se"));
    }

    /**
     * Asserts that generated query equals the expected generated query.
     * Asserts that generated filter equals the expected filter.
     *
     * @param query         The query.
     * @param wantedQuery   The expected query.
     * @param wantedFilter  The expected filter.
     */
    private void executeTestOfQuery(final String query, final String wantedQuery, final String wantedFilter) {
        final SearchCommand.Context cxt = createCommandContext(query, "d", "yahoo-image-search");
        final AbstractYahooSearchCommand cmd = new YahooMediaSearchCommand(cxt, new Hashtable<String,Object>());
        final String generatedQuery = cmd.getQueryRepresentation(cxt.getQuery());
        assertEquals("Generated query does not match wanted query", wantedQuery, generatedQuery.trim());
        assertEquals("Generated filter does not match wanter filter", wantedFilter, cmd.getAdditionalFilter());
    }
}
