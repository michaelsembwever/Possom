// Copyright (2006) Schibsted Søk AS
/*
 * WebSearchCommandTest.java
 * JUnit based test
 *
 * Created on March 7, 2006, 4:53 PM
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Collections;

/**
 *
 * @author magnuse
 */
public final class WebSearchCommandTest extends AbstractSearchCommandTest {

    public WebSearchCommandTest(final String testName) {
        super(testName);
    }

    /**
     * Test of the site prefix.
     */
    public void testSiteFilter() {
        executeTestOfQuery(
                "site:zmag.org bil",
                "bil",
                "+site:zmag.org");
    }

    /**
     * Test of the site prefix whith quotes.
     */
    public void testSiteFilterWithQuotes() {
        executeTestOfQuery(
                "site:\"zmag.org\" bil",
                "bil",
                "+site:zmag.org");
    }

    /**
     *
     * Make sure that that phrase searches works.
     */
    public void testPhraseSearches() {
        executeTestOfQuery(
                "\"george bush\"",
                "\"george bush\"",
                "");
    }

    /** Test that the nyhetskilde prefix is ignored.
     */
    public void testIgnoreField() {
        executeTestOfQuery(
                "nyhetskilde:vg bil",
                "bil",
                "");
    }

    public void testExclusion() {
        executeTestOfQuery("magnus -eklund",
                "magnus -eklund",
                "");
        executeTestOfQuery("-whatever",
                "-whatever",
                "");
    }


    public void testTwoTerms() {
        executeTestOfQuery("magnus eklund",
                "magnus eklund",
                "");
    }

    /**
     *
     *
     */
    public void testSiteExclusion() {
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
    public void testPhoneNumberSearches() {
        executeTestOfQuery(
                "97 40 33 06",
                "97 40 33 06",
                "");
    }

    private void executeTestOfQuery(final String query, final String wantedQuery, final String wantedFilter) {

        final SearchCommand.Context cxt = createCommandContext(query, "d", "defaultSearch");

        final WebSearchCommand cmd = new WebSearchCommand(cxt, Collections.EMPTY_MAP);

        final String generatedQuery = cmd.getQueryRepresentation(cxt.getQuery());

        assertEquals(wantedQuery, generatedQuery.trim());
        assertEquals(wantedFilter, cmd.getAdditionalFilter());
    }

}
