package no.schibstedsok.front.searchportal.connectors.test;

import java.util.Iterator;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.command.OvertureConnectorCommand;
import no.schibstedsok.front.searchportal.response.CommandResponse;

public class OvertureConnectorTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(OvertureConnectorTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDoExecute() {
        
        OvertureConnectorCommand command = new OvertureConnectorCommand();
        
        command.setQueryString("Schibsted");
        command.setDirective("search");
        command.setQueryString("Schibsted");
        
        command.execute();
        assertNotNull(command.getResponse());
        
        CommandResponse response = command.getResponse();
                
        for (Iterator i = response.getResults().iterator(); i.hasNext(); ) {
            System.out.println(i.next());
        }
        
    }

}
