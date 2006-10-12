// Copyright (2006) Schibsted Søk AS
/*
 * WebSearchCommandTest.java
 * JUnit based test
 *
 * Created on March 7, 2006, 4:53 PM
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.Collections;
import org.testng.annotations.Test;

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
    @Test
    public void testSiteFilter() {
        executeTestOfQuery(
                "site:zmag.org bil",
                "bil",
                "+site:zmag.org");
    }

    /**
     * Test of the site prefix whith quotes.
     */
    @Test
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
    @Test
    public void testPhraseSearches() {
        executeTestOfQuery(
                "\"george bush\"",
                "\"george bush\"",
                "");
    }

    /** Test that the nyhetskilde prefix is ignored.
     */
    @Test
    public void testIgnoreField() {
        executeTestOfQuery(
                "nyhetskilde:vg bil",
                "bil",
                "");
    }

    @Test
    public void testExclusion() {
        executeTestOfQuery("magnus -eklund",
                "magnus -eklund",
                "");
        executeTestOfQuery("-whatever",
                "-whatever",
                "");
    }


    @Test
    public void testTwoTerms() {
        executeTestOfQuery("magnus eklund",
                "magnus eklund",
                "");
    }

    /**
     *
     *
     */
    @Test
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
    @Test
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
