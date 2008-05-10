/* Copyright (2006-2007) Schibsted Søk AS
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
/*
 * WebSearchCommandTest.java
 *
 * Created on March 7, 2006, 4:53 PM
 */

package no.sesat.search.mode.command;


import no.sesat.search.mode.command.*;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 *
 *
 * @version $Id$
 */
public final class WebSearchCommandTest extends AbstractSearchCommandTest {

    /**
     * Test of the site prefix.
     */
    @Test
    public void testSiteFilter()  throws Exception{
        executeTestOfQuery(
                "site:zmag.org bil",
                "bil",
                "+site:zmag.org");
    }

    /**
     * Test of the site prefix whith quotes.
     */
    @Test
    public void testSiteFilterWithQuotes()  throws Exception{
        executeTestOfQuery(
                "site:\"zmag.org\" bil",
                "bil",
                "+site:zmag.org");
    }

    /**
     *
     * Make sure that that phrase searches works.
     */
    @Test
    public void testPhraseSearches()  throws Exception{
        executeTestOfQuery(
                "\"george bush stands on a chair to raise his IQ\"",
                "\"george bush stands on a chair to raise his IQ\"",
                "");
    }

    /** Test that the nyhetskilde prefix is escaped.
     */
    @Test
    public void testIgnoreField()  throws Exception{
        executeTestOfQuery(
                "nyhetskilde:vg bil",
                "nyhetskilde\\:vg bil",
                "");
    }

    @Test
    public void testExclusion()  throws Exception{
        executeTestOfQuery("magnus -eklund",
                "magnus -eklund",
                "");
        executeTestOfQuery("-whatever",
                "-whatever",
                "");
    }


    @Test
    public void testTwoTerms()  throws Exception{
        executeTestOfQuery("magnus eklund",
                "magnus eklund",
                "");
    }

    /**
     *
     *
     */
    @Test
    public void testSiteExclusion()  throws Exception{
//        executeTestOfQuery(
//                "-site:zmag.org bil",
//                "bil",
//                "-site:zmag.org"
//                );
//
//        executeTestOfQuery(
//                "NOT site:zmag.org bil",
//                "bil",
//                "-site:zmag.org"
//                );
    }

    /**
     *
     * Make sure phone numbers are not normalized.
     *
     */
    @Test
    public void testPhoneNumberSearches()  throws Exception{
        executeTestOfQuery(
                "97 40 33 06",
                "97 40 33 06",
                "");
    }

    private void executeTestOfQuery(
            final String query,
            final String wantedQuery,
            final String wantedFilter) throws SiteKeyedFactoryInstantiationException{

        final SearchCommand.Context cxt = createCommandContext(query, "d", "defaultSearch");

        final WebSearchCommand cmd = new WebSearchCommand(cxt);

        final String generatedQuery = cmd.getQueryRepresentation(cxt.getDataModel().getQuery().getQuery());

        assertEquals(wantedQuery, generatedQuery.trim());
        assertEquals(wantedFilter, cmd.getAdditionalFilter());
    }

}
