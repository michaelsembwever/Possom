/* Copyright (2008) Schibsted Søk AS
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
 */
package no.sesat.search.mode.command;


import java.util.Collections;
import no.sesat.search.mode.command.*;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.config.SearchTab.EnrichmentHint;
import no.sesat.search.view.config.SearchTab.EnrichmentPlacementHint;
import no.sesat.search.view.config.SearchTab.Layout;
import no.sesat.search.view.config.SearchTab.Scope;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * Tests for the AdvancedFastSearchCommand.
 */
public class AdvancedFastSearchCommandTest extends AbstractSearchCommandTest {

    /**
     * Test a single term.
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testSingleTerm()  throws Exception{
        executeTestOfQuery("test", "test", "");
    }

    /**
     * Test two terms.
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testTwoTerms()  throws Exception{
        executeTestOfQuery("october surprise", "october AND surprise", "");
        executeTestOfQuery("+october +surprise", "october AND surprise", "");
    }

    /**
     * Test three terms.
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testThreeTerms()  throws Exception{
        executeTestOfQuery("xyz zyx yxz", "xyz AND zyx AND yxz", "");
    }

    /**
     * Test OR operator.
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testOr()  throws Exception{
        executeTestOfQuery("(october surprise)", "(october OR surprise)", "");
        executeTestOfQuery("(october surprise) (test test1)", "(october OR surprise) AND (test OR test1)", "");
    }

    /**
     * Test NOT operator.
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testNot()  throws Exception{
        executeTestOfQuery("october -surprise", "october ANDNOT surprise", "");
        executeTestOfQuery("october -surprise -whatever", "october ANDNOT surprise ANDNOT whatever", "");
        executeTestOfQuery("october -surprise whatever -more", "october ANDNOT surprise AND whatever ANDNOT more", "");
    }

    /**
     * Test NOT operator as first token of query.
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testLeadingNot()  throws Exception{
        executeTestOfQuery("-surprise october", "#ANDNOT surprise AND october", "");
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

        final SearchTab fakeTab = new SearchTab(null, "fake-view", "default-mode", "test", null, null, true,
                null, Collections.<EnrichmentPlacementHint>emptyList(), Collections.<EnrichmentHint>emptyList(), null,
                0, 0, Collections.<String>emptyList(), Collections.<String>emptyList(), false, false,
                null, Collections.<String,Layout>emptyMap(), Scope.REQUEST);

        final SearchCommand.Context cxt = createCommandContext(query, fakeTab, "default-advanced-fast-command");
        @SuppressWarnings("deprecation")
        final AbstractSearchCommand cmd = new AdvancedFastSearchCommand(cxt);
        final String generatedQuery = cmd.getQueryRepresentation();
        assertEquals("Generated query does not match wanted query", wantedQuery, generatedQuery.trim());
        assertEquals("Generated filter does not match wanter filter", wantedFilter, cmd.getFilter());
    }
}
