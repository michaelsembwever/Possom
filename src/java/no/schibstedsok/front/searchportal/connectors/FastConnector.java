/**
 * 
 */
package no.schibstedsok.front.searchportal.connectors;

import no.fast.ds.search.ConfigurationException;
import no.fast.ds.search.FastSearchEngineFactory;
import no.fast.ds.search.IFastSearchEngineFactory;
import no.schibstedsok.front.searchportal.command.ConnectorCommand;
import no.schibstedsok.front.searchportal.connectors.interfaces.Connector;

import org.apache.log4j.Logger;

/**
 * @author Lars Johansson
 * 
 */
public class FastConnector extends BaseConnector  {

    Logger log = Logger.getLogger(this.getClass());
	
	public IFastSearchEngineFactory factory = null;

    public static FastConnector instance = null;

    /**
     * 
     */
    protected FastConnector() {
        super();
        try {
            
            factory = FastSearchEngineFactory.newInstance();

        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void doExecute(ConnectorCommand command) {
        command.execute();
    }


	/** 
	 * 
	 * @param configuration
	 * @return
	 */
	public static synchronized Connector getInstance() {
       
		if(instance == null)
            instance = new FastConnector();
        return instance;
	}

	
}
