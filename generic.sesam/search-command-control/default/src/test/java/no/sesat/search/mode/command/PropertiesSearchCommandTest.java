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
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.config.SearchTab.EnrichmentHint;
import no.sesat.search.view.config.SearchTab.EnrichmentPlacementHint;
import no.sesat.search.view.config.SearchTab.Layout;
import no.sesat.search.view.config.SearchTab.Scope;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 *
 * @version $Id$
 */
public class PropertiesSearchCommandTest extends AbstractSearchCommandTest {

    @Test
    public void testSingleTerm()  throws Exception{
        executeTestOfQuery("test");
    }


    @Test
    public void testTwoTerms()  throws Exception{
        executeTestOfQuery("test1 test2");
    }

    @Test
    public void testThreeTerms()  throws Exception{
        executeTestOfQuery("test1 test2 test3");
    }

    @Test
    public void testTwoTermsPlus()  throws Exception{
        executeTestOfQuery("+test1 +test2");
    }


    @Test
    public void testNot()  throws Exception{
        executeTestOfQuery("test1 -test2");
    }

    @Test
    public void testLeadingNot()  throws Exception{
        executeTestOfQuery("-test1 test2");
    }

    @Test
    public void testOr()  throws Exception{
        executeTestOfQuery("(test1 test2)");
    }

    @Test
    public void testLeadingNotAndOr()  throws Exception{
        executeTestOfQuery("-a (test1 test2)");
    }

    @Test
    public void testLotsOfNots()  throws Exception{
        executeTestOfQuery("-a d -b -e c -f");
        executeTestOfQuery("-a d -b -e c g -f");
        executeTestOfQuery("-a -b c");
        executeTestOfQuery("-a (e f) c");
    }


    @Test
    public void testSiteRestriction() throws Exception{

        executeTestOfQuery("site:aftonbladet.se banan");
        executeTestOfQuery("banan site:aftonbladet.se");
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
            final String query)  throws SiteKeyedFactoryInstantiationException{

        final SearchTab fakeTab = new SearchTab(null, "fake-view", "default-mode", "test", null, null, true,
                null, Collections.<EnrichmentPlacementHint>emptyList(), Collections.<EnrichmentHint>emptyList(), null,
                0, 0, Collections.<String>emptyList(), Collections.<String>emptyList(), false, false,
                null, Collections.<String,Layout>emptyMap(), Scope.REQUEST);

        final SearchCommand.Context cxt = createCommandContext(query, fakeTab, "default-properties-command");
        final PropertiesCommand cmd = new PropertiesCommand(cxt);
        final String generatedQuery = cmd.getQueryRepresentation();
        assertEquals("Generated query does not match wanted query", query.toLowerCase(), generatedQuery.trim());
    }
}
