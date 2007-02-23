/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/**
 * Test class for <code>YelloGeoSearch</code>.
 *
 * @author <a href="mailto:endre@sesam.no">Endre Midtgård Meckelborg</a>
 * @version <tt>$Revision: $</tt>
 */
public final class YellowGeoSearchTest extends AbstractSearchCommandTest {

    @Test
    public void testQueryRepresentationOrganisationNumber()  throws Exception{
        final String query = getParsedQueryAsString("933739384");
        assertEquals("yellowpages:933739384", query);
    }

    private String getParsedQueryAsString(final String query) throws SiteKeyedFactoryInstantiationException {
        
        final SearchCommand.Context cxt = createCommandContext(query, "y", "yellowPages");
        final YellowSearchCommand command = createSearchCommand(cxt);
        return command.getQueryRepresentation(cxt.getQuery());

    }

    private YellowSearchCommand createSearchCommand(final SearchCommand.Context cxt) throws SiteKeyedFactoryInstantiationException {
        return new YellowSearchCommand(cxt, getDataModel());
    }

}
