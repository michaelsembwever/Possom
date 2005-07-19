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
public abstract class BaseConnector implements Connector {

    public void execute(ConnectorCommand command, SearchConfiguration configuration) {
        
		command.setConfiguration(configuration);
		doExecute(command);
		
    }

    protected abstract void doExecute(ConnectorCommand command);    
	
}
