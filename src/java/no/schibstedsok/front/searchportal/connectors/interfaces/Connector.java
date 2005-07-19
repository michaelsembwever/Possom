/**
 * 
 */
package no.schibstedsok.front.searchportal.connectors.interfaces;

import no.schibstedsok.front.searchportal.command.ConnectorCommand;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

/**
 * @author Lars Johansson
 *
 */
public interface Connector {

    public void execute(ConnectorCommand command, SearchConfiguration configuration);
}
