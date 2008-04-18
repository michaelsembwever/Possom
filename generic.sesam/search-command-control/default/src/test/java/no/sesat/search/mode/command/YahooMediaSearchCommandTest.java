/* Copyright (2007) Schibsted Søk AS
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


import no.sesat.search.mode.command.*;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import static org.testng.AssertJUnit.*;


/**
 *
 */
public class YahooMediaSearchCommandTest extends AbstractSearchCommandTest {

    /**
     * Test a single term.
     */
    public void testSingleTerm()  throws Exception{
        executeTestOfQuery("test", "test", "");
    }


    public void testTwoTerms()  throws Exception{
        executeTestOfQuery("test1 test2", "test1 AND test2", "");
    }

    public void testThreeTerms()  throws Exception{
        executeTestOfQuery("test1 test2 test3", "test3 AND test1 AND test2", "");
    }

    public void testTwoTermsPlus()  throws Exception{
        executeTestOfQuery("+test1 +test2", "test1 AND test2", "");
    }


    public void testNot()  throws Exception{
        executeTestOfQuery("test1 -test2", "test1 ANDNOT test2", "");
    }

    public void testLeadingNot()  throws Exception{
        executeTestOfQuery("-test1 test2", "test2 ANDNOT test1", "");
    }

    public void testOr()  throws Exception{
        executeTestOfQuery("(test1 test2)", "(test1 OR test2)", "");
    }

    public void testLeadingNotAndOr()  throws Exception{
        executeTestOfQuery("-a (test1 test2)", "(test1 OR test2) ANDNOT a", "");
    }

    public void testLotsOfNots()  throws Exception{
        executeTestOfQuery("-a d -b -e c -f", "c AND d ANDNOT a ANDNOT b ANDNOT e ANDNOT f", "");
        executeTestOfQuery("-a d -b -e c g -f", "g AND c AND d ANDNOT a ANDNOT b ANDNOT e ANDNOT f", "");
        executeTestOfQuery("-a -b c", "c ANDNOT a ANDNOT b", "");
        executeTestOfQuery("-a (e f) c", "c AND (e OR f) ANDNOT a", "");
    }


    public void testSiteRestriction() throws Exception{

        executeTestOfQuery("site:aftonbladet.se banan", "banan", "");
        executeTestOfQuery("banan site:aftonbladet.se", "banan", "");

        final SearchCommand.Context cxt
                = createCommandContext("site:aftonbladet.se banan", "d", "yahoo-image-search");
        final AbstractYahooSearchCommand cmd = new YahooMediaSearchCommand(cxt);
        cmd.getQueryRepresentation(cxt.getDataModel().getQuery().getQuery());
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
    private void executeTestOfQuery(
            final String query,
            final String wantedQuery,
            final String wantedFilter)  throws SiteKeyedFactoryInstantiationException{

        final SearchCommand.Context cxt = createCommandContext(query, "d", "yahoo-image-search");
        final AbstractYahooSearchCommand cmd = new YahooMediaSearchCommand(cxt);
        final String generatedQuery = cmd.getQueryRepresentation(cxt.getDataModel().getQuery().getQuery());
        assertEquals("Generated query does not match wanted query", wantedQuery, generatedQuery.trim());
        assertEquals("Generated filter does not match wanter filter", wantedFilter, cmd.getAdditionalFilter());
    }
}