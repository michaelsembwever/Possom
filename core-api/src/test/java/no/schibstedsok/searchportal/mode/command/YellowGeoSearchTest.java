/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;


import java.util.Hashtable;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;
   

/**
 * Test class for <code>YelloGeoSearch</code>.
 *
 * @author <a href="mailto:endre@sesam.no">Endre Midtgård Meckelborg</a>
 * @version <tt>$Revision: $</tt>
 */
public final class YellowGeoSearchTest extends AbstractSearchCommandTest {

    public YellowGeoSearchTest(final String name) {
        super(name);
    }

    @Test
    public void testQueryRepresentationOrganisationNumber() {
        final String query = getParsedQueryAsString("933739384");
        assertEquals("yellowpages:933739384", query);
    }

    private String getParsedQueryAsString(final String query) {
        final SearchCommand.Context cxt = createCommandContext(query, "y", "yellowPages");
        final YellowSearchCommand command = createSearchCommand(cxt);
        return command.getQueryRepresentation(cxt.getQuery());

    }

    private YellowSearchCommand createSearchCommand(final SearchCommand.Context cxt) {
        return new YellowSearchCommand(cxt, new Hashtable<String,Object>());
    }

}
