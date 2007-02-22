// Copyright (2006) Schibsted Søk AS
/*
 *
 * Created on March 4, 2006, 2:32 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;


import java.util.Hashtable;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/**
 *
 * @author magnuse
 */
public final class WhiteSearchCommandTest extends AbstractSearchCommandTest {

    public WhiteSearchCommandTest(final String name) {
        super(name);
    }

    @Test
    public void testQueryRepresentationInteger() {
        final String query = getParsedQueryAsString("524287");
        assertEquals("whitepages:524287", query);
    }

    @Test
    public void testQueryRepresentationWord() {
        final String query = getParsedQueryAsString("word");
        assertEquals("whitephon:word", query);
    }

    @Test
    public void testQueryRepresentationTwoWords() {
        final String query = getParsedQueryAsString("word word2");
        assertEquals("whitephon:word AND whitephon:word2", query);
    }

    @Test
    public void testQueryRepresentationPhoneNumber() {
        final String query = getParsedQueryAsString("97403306");
        assertEquals("whitepages:97403306", query);

        final String queryWithSpaces = getParsedQueryAsString("97 40 33 06");
        assertEquals("whitepages:97403306", queryWithSpaces);
    }

    @Test
    public void testQueryRepresentationPhrase() {
        final String query = getParsedQueryAsString("\"magnus eklund\"");
        assertEquals("whitephon:magnus AND whitephon:eklund", query);
    }

    @Test
    public void testQueryRepresentationMixed() {
        final String query = getParsedQueryAsString("\"magnus eklund\" 97 40 3306 oslo sarsgate 74");
        assertEquals("whitephon:magnus AND whitephon:eklund AND whitepages:97403306 AND whitephon:oslo AND whitephon:sarsgate AND whitepages:74", query);
    }

    @Test
    public void testIgnoreField() {
        final String query = getParsedQueryAsString("site:vg.no magnus eklund");
        assertEquals("whitephon:magnus AND whitephon:eklund", query.trim());
    }

    private String getParsedQueryAsString(final String query) {
        final SearchCommand.Context cxt = createCommandContext(query, "w", "whitePages");
        final WhiteSearchCommand command = createSearchCommand(cxt);
        return command.getQueryRepresentation(cxt.getQuery());

    }

    private WhiteSearchCommand createSearchCommand(final SearchCommand.Context cxt) {
        return new WhiteSearchCommand(cxt, new Hashtable<String,Object>());
    }

}