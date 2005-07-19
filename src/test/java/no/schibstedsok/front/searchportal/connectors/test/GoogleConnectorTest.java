package no.schibstedsok.front.searchportal.connectors.test;

import java.util.Iterator;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.command.GoogleConnectorCommand;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.connectors.GoogleConnector;
import no.schibstedsok.front.searchportal.connectors.interfaces.Connector;
import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

public class GoogleConnectorTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(GoogleConnectorTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDoExecute() {
        
        Connector connector = new GoogleConnector();
        GoogleConnectorCommand command = new GoogleConnectorCommand();
		SearchConfiguration configuration = new FastSearchConfigurationImpl();

        command.setQueryString("Schibsted ASA");
        command.setDirective("search");
        
        connector.execute(command, null);
        assertNotNull(command.getResponse());
        
        CommandResponse response = command.getResponse();
                
        for (Iterator i = response.getResults().iterator(); i.hasNext(); ) {
            System.out.println(i.next());
        }
        
    }

}
