package no.schibstedsok.front.searchportal.connectors.test;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.command.ConnectorCommand;
import no.schibstedsok.front.searchportal.command.SensisConnectorCommand;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SensisFastSearchConfiguration;
import no.schibstedsok.front.searchportal.connectors.FastConnector;
import no.schibstedsok.front.searchportal.response.FastSearchResponseImpl;

public class SensisConnectorTest extends TestCase {

	FastSearchConfiguration configuration = null; 

	public static void main(String[] args) {
        junit.textui.TestRunner.run(SensisConnectorTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
		configuration = new SensisFastSearchConfiguration();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDoQuery() {

        
        ConnectorCommand command = new SensisConnectorCommand();

        configuration.setQuery("schibsted");
        configuration.setDocsToReturn(10);

        FastConnector.getInstance().execute(command, configuration);
        assertNotNull(command.getResponse());
        
        System.out.println(command.getResponse());

    }

    public void _testSpellingSuggestion() {

        ConnectorCommand command = new SensisConnectorCommand();

        configuration.setQuery("arnsen");

		FastConnector.getInstance().execute(command, configuration);

        FastSearchResponseImpl response = (FastSearchResponseImpl) command.getResponse();
        
        System.out.println(response);

        assertNotNull(response);
//        assertNotSame("", response.getSpellingSuggestion());

    }

}
