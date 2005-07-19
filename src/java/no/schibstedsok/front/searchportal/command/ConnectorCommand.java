/**
 * 
 */
package no.schibstedsok.front.searchportal.command;

import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

/**
 * @author Lars Johansson
 * 
 */
public interface ConnectorCommand {

    public CommandResponse getResponse();

	public void setConfiguration(SearchConfiguration config);
	
    public void execute();

}
