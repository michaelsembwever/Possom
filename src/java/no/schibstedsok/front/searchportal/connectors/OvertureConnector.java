/**
 * 
 */
package no.schibstedsok.front.searchportal.connectors;

import no.schibstedsok.front.searchportal.command.ConnectorCommand;
import no.schibstedsok.front.searchportal.connectors.interfaces.Connector;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

/**
 * @author Lars Johansson
 *
 */
public class OvertureConnector implements Connector {

    /**
     * 
     */
    public OvertureConnector() {
        super();
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.connectors.interfaces.Connector#execute(com.schibstedsok.portal.search.command.ConnectorCommand)
     */
    public void execute(ConnectorCommand command, SearchConfiguration configuration) {
        command.execute();
    }

}
