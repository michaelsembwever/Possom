package no.schibstedsok.searchportal.mode.command;

import java.util.Collections;
import org.testng.annotations.Test;

/**
 * Tests for the AdvancedFastSearchCommand.
 */
public class AdvancedFastSearchCommandTest extends AbstractSearchCommandTest {

    /**
     * Creates a new instance of the test.
     *
     * @param testName The name of the test.
     */
    public AdvancedFastSearchCommandTest(String testName) {
        super(testName);
    }

    /**
     * Test a single term.
     */
    @Test
    public void testSingleTerm() {
        executeTestOfQuery("test", "test", "");
    }

    /**
     * Test two terms.
     */
    @Test
    public void testTwoTerms() {
        executeTestOfQuery("october surprise", "october AND surprise", "");
        executeTestOfQuery("+october +surprise", "october AND surprise", "");
    }

    /**
     * Test three terms.
     */
    @Test
    public void testThreeTerms() {
        executeTestOfQuery("xyz zyx yxz", "xyz AND zyx AND yxz", "");
    }

    /**
     * Test OR operator.
     */
    @Test
    public void testOr() {
        executeTestOfQuery("(october surprise)", "(october OR surprise)", "");
        executeTestOfQuery("(october surprise) (test test1)", "(october OR surprise) AND (test OR test1)", "");
    }

    /**
     * Test NOT operator.
     */
    @Test
    public void testNot() {
        executeTestOfQuery("october -surprise", "october ANDNOT surprise", "");
        executeTestOfQuery("october -surprise -whatever", "october ANDNOT surprise ANDNOT whatever", "");
        executeTestOfQuery("october -surprise whatever -more", "october ANDNOT surprise AND whatever ANDNOT more", "");
    }

    /**
     * Test NOT operator as first token of query.
     */
    @Test
    public void testLeadingNot() {
        executeTestOfQuery("-surprise october", "# ANDNOT surprise AND october", "");
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
        final SearchCommand.Context cxt = createCommandContext(query, "d", "defaultSearch");
        final AbstractAdvancedFastSearchCommand cmd = new AdvancedFastSearchCommand(cxt, Collections.EMPTY_MAP);
        final String generatedQuery = cmd.getQueryRepresentation(cxt.getQuery());
        assertEquals("Generated query does not match wanted query", wantedQuery, generatedQuery.trim());
        assertEquals("Generated filter does not match wanter filter", wantedFilter, cmd.getAdditionalFilter());
    }
}
