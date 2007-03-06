/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.command;


import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/**
 * Test class for <code>YelloGeoSearch</code>.
 *
 * @author <a href="mailto:endre@sesam.no">Endre Midtgård Meckelborg</a>
 * @version <tt>$Id$</tt>
 */
public final class YellowGeoSearchTest extends AbstractSearchCommandTest {

    @Test
    public void testQueryRepresentationOrganisationNumber()  throws Exception{
        final String query = getParsedQueryAsString("933739384");
        assertEquals("yellowpages:933739384", query);
    }

    private String getParsedQueryAsString(final String query) throws SiteKeyedFactoryInstantiationException {
        
        final DataModel datamodel = getDataModel();
        
        final SearchCommand.Context cxt = createCommandContext(query, "y", datamodel, "yellowPages");
        final YellowSearchCommand command = createSearchCommand(cxt, datamodel);
        return command.getQueryRepresentation(datamodel.getQuery().getQuery());

    }

    private YellowSearchCommand createSearchCommand(
            final SearchCommand.Context cxt,
            final DataModel datamodel) throws SiteKeyedFactoryInstantiationException {
        
        return new YellowSearchCommand(cxt, datamodel);
    }

}
