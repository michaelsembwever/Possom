package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.mode.config.YahooMediaSearchConfiguration;

import java.util.Collections;

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
    /**
     * Asserts that generated query equals the expected generated query.
     * Asserts that generated filter equals the expected filter.
     *
     * @param query         The query.
     * @param wantedQuery   The expected query.
     * @param wantedFilter  The expected filter.
     */
    private void executeTestOfQuery(final String query, final String wantedQuery, final String wantedFilter) {
        final SearchCommand.Context cxt = createCommandContext(query, "d", "yahoo-media-command");
        final AbstractYahooSearchCommand cmd = new YahooMediaSearchCommand(cxt, Collections.EMPTY_MAP);
        final String generatedQuery = cmd.getQueryRepresentation(cxt.getQuery());
        assertEquals("Generated query does not match wanted query", wantedQuery, generatedQuery.trim());
        assertEquals("Generated filter does not match wanter filter", wantedFilter, cmd.getAdditionalFilter());
    }
}
