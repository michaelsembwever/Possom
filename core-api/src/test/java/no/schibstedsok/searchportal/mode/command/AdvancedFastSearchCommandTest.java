package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * Tests for the AdvancedFastSearchCommand.
 */
public class AdvancedFastSearchCommandTest extends AbstractSearchCommandTest {

    /**
     * Test a single term.
     */
    @Test
    public void testSingleTerm()  throws Exception{
        executeTestOfQuery("test", "test", "");
    }

    /**
     * Test two terms.
     */
    @Test
    public void testTwoTerms()  throws Exception{
        executeTestOfQuery("october surprise", "october AND surprise", "");
        executeTestOfQuery("+october +surprise", "october AND surprise", "");
    }

    /**
     * Test three terms.
     */
    @Test
    public void testThreeTerms()  throws Exception{
        executeTestOfQuery("xyz zyx yxz", "xyz AND zyx AND yxz", "");
    }

    /**
     * Test OR operator.
     */
    @Test
    public void testOr()  throws Exception{
        executeTestOfQuery("(october surprise)", "(october OR surprise)", "");
        executeTestOfQuery("(october surprise) (test test1)", "(october OR surprise) AND (test OR test1)", "");
    }

    /**
     * Test NOT operator.
     */
    @Test
    public void testNot()  throws Exception{
        executeTestOfQuery("october -surprise", "october ANDNOT surprise", "");
        executeTestOfQuery("october -surprise -whatever", "october ANDNOT surprise ANDNOT whatever", "");
        executeTestOfQuery("october -surprise whatever -more", "october ANDNOT surprise AND whatever ANDNOT more", "");
    }

    /**
     * Test NOT operator as first token of query.
     */
    @Test
    public void testLeadingNot()  throws Exception{
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
    private void executeTestOfQuery(
            final String query,
            final String wantedQuery,
            final String wantedFilter) throws SiteKeyedFactoryInstantiationException {

        final SearchCommand.Context cxt = createCommandContext(query, "d", "defaultSearch");
        final AbstractAdvancedFastSearchCommand cmd = new AdvancedFastSearchCommand(cxt, getDataModel());
        final String generatedQuery = cmd.getQueryRepresentation(cxt.getQuery());
        assertEquals("Generated query does not match wanted query", wantedQuery, generatedQuery.trim());
        assertEquals("Generated filter does not match wanter filter", wantedFilter, cmd.getAdditionalFilter());
    }
}
