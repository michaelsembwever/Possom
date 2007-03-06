// Copyright (2006-2007) Schibsted Søk AS
/*
 *
 * Created on March 4, 2006, 2:32 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/**
 *
 * @author magnuse
 */
public final class WhiteSearchCommandTest extends AbstractSearchCommandTest {

    @Test
    public void testQueryRepresentationInteger()  throws Exception{
        final String query = getParsedQueryAsString("524287");
        assertEquals("whitepages:524287", query);
    }

    @Test
    public void testQueryRepresentationWord()  throws Exception{
        final String query = getParsedQueryAsString("word");
        assertEquals("whitephon:word", query);
    }

    @Test
    public void testQueryRepresentationTwoWords()  throws Exception{
        final String query = getParsedQueryAsString("word word2");
        assertEquals("whitephon:word AND whitephon:word2", query);
    }

    @Test
    public void testQueryRepresentationPhoneNumber()  throws Exception{
        final String query = getParsedQueryAsString("97403306");
        assertEquals("whitepages:97403306", query);

        final String queryWithSpaces = getParsedQueryAsString("97 40 33 06");
        assertEquals("whitepages:97403306", queryWithSpaces);
    }

    @Test
    public void testQueryRepresentationPhrase()  throws Exception{
        final String query = getParsedQueryAsString("\"magnus eklund\"");
        assertEquals("whitephon:magnus AND whitephon:eklund", query);
    }

    @Test
    public void testQueryRepresentationMixed()  throws Exception{
        final String query = getParsedQueryAsString("\"magnus eklund\" 97 40 3306 oslo sarsgate 74");
        assertEquals("whitephon:magnus AND whitephon:eklund AND whitepages:97403306 AND whitephon:oslo AND whitephon:sarsgate AND whitepages:74", query);
    }

    @Test
    public void testIgnoreField()  throws Exception{
        final String query = getParsedQueryAsString("site:vg.no magnus eklund");
        assertEquals("whitephon:magnus AND whitephon:eklund", query.trim());
    }

    private String getParsedQueryAsString(final String query) throws SiteKeyedFactoryInstantiationException {

        final DataModel datamodel = getDataModel();
        
        final SearchCommand.Context cxt = createCommandContext(query, "w", datamodel, "whitePages");
        final WhiteSearchCommand command = createSearchCommand(cxt, datamodel);
        return command.getQueryRepresentation(datamodel.getQuery().getQuery());

    }

    private WhiteSearchCommand createSearchCommand(
            final SearchCommand.Context cxt,
            final DataModel datamodel) throws SiteKeyedFactoryInstantiationException {

        return new WhiteSearchCommand(cxt, datamodel);
    }

}