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
 * Fast4SearchCommandTest.java
 *
 * Created on March 7, 2006, 4:53 PM
 */

package no.sesat.search.mode.command;


import java.util.Collections;
import no.sesat.search.mode.command.*;
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
 *
 * @version $Id$
 */
public final class Fast4SearchCommandTest extends AbstractSearchCommandTest {

    /**
     * Test of the site prefix.
     * @throws Exception just throw any problem. it's a test
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
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testSiteFilterWithQuotes()  throws Exception{
        executeTestOfQuery(
                "site:\"zmag.org\" bil",
                "bil",
                "+site:\"zmag.org\"");
    }

    /**
     *
     * Make sure that that phrase searches works.
     * @throws Exception just throw any problem. it's a test
     */
    @Test
    public void testPhraseSearches()  throws Exception{
        executeTestOfQuery(
                "\"george bush stands on a chair to raise his IQ\"",
                "\"george bush stands on a chair to raise his iq\"",
                "");
    }

    /** Test that the nyhetskilde prefix is escaped.
     * @throws Exception just throw any problem. it's a test
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

        final SearchTab fakeTab = new SearchTab(null, "fake-view", "default-mode", "test", null, null, true,
                null, Collections.<EnrichmentPlacementHint>emptyList(), Collections.<EnrichmentHint>emptyList(), null,
                0, 0, Collections.<String>emptyList(), Collections.<String>emptyList(), false, false,
                null, Collections.<String,Layout>emptyMap(), Scope.REQUEST);

        final SearchCommand.Context cxt = createCommandContext(query, fakeTab, "default-fast-command");
        final WebSearchCommand cmd = new WebSearchCommand(cxt);

        final String generatedQuery = cmd.getQueryRepresentation();

        assertEquals(wantedQuery, generatedQuery.trim());
        assertEquals(wantedFilter, cmd.getFilter());
    }

}
