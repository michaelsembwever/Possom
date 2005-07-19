/**
 * 
 */
package no.schibstedsok.front.searchportal.connectors;

import no.schibstedsok.front.searchportal.command.ConnectorCommand;


/**
 * 
 * Do all Google connection specific configuration here. 
 * 
 * @author Lars Johansson
 *
 */
public class GoogleConnector extends BaseConnector {

    public static final String googleKey = "udsuTNdQFHKkcsPVqdd1DU7yITnoXtAA";

    /**
     * 
     */
    public GoogleConnector() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doExecute(ConnectorCommand command) {
        command.execute();
    }
    

}
