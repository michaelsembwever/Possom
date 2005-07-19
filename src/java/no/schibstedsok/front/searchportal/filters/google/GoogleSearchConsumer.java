/**
 * 
 */
package no.schibstedsok.front.searchportal.filters.google;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.command.GoogleConnectorCommand;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.connectors.GoogleConnector;
import no.schibstedsok.front.searchportal.connectors.interfaces.Connector;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.response.SearchResultElement;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

/**
 * @author Lars Johansson
 * 
 */
public class GoogleSearchConsumer extends SearchConsumer {

	SearchConfiguration config = null;
    
	/**
     * @param query
     * @param maxWaitTime
     * @param response
     */
    public GoogleSearchConsumer(String query, long maxWaitTime, ServletResponse response, int maxResults, int offset) {
		
		super(response, new FastSearchConfigurationImpl());
		
		FastSearchConfigurationImpl config = new FastSearchConfigurationImpl();
		config.setQuery(query);
		config.setMaxTime(maxWaitTime);
		config.setDocsToReturn(maxResults);
		config.setOffSet(offset);
        
		
    }
	
	/**
	 * Create a new GoogleSearchConsumer.
	 * 
	 * @param response
	 * @param config
	 */
	public GoogleSearchConsumer(Writer response, SearchConfiguration config) {
		// FIXME GoogleSearchConsumer constructor
		super(response, config);
	}



	public void run() {
        {
            SearchThread w = new SearchThread(config.getQuery(), myResponseRef);
            final Thread googleThread = new Thread(w);

            googleThread.start();

            final Object lock = new Object();
            synchronized (lock) {
                try {
                    lock.wait(((FastSearchConfiguration) config).getMaxTime());
//                    System.out.println("SearchConsumer waited " + config.getMaxTime()                           + " msec");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            googleThread.interrupt();
        }
    }

    
    /**
     * 
     * Inner class implementing the Google search
     * 
     * 
     * @author Lars Johansson
     *
     */
    public class SearchThread implements Runnable {

        Connector connector = new GoogleConnector();
		GoogleConnectorCommand googleCommand = new GoogleConnectorCommand();

        ServletResponse myResponseRef = null;

        private String myQuery = null;

        public SearchThread(String query, ServletResponse response) {
            myQuery = query;
            myResponseRef = response;
        }

        public void run() {
            try {
                googleCommand.setDirective("search");
                googleCommand.setQueryString(myQuery);

                // connect and execute search!
                connector.execute(googleCommand,null);

                CommandResponse searchResponse = googleCommand.getResponse();

                // TODO: What happens with the response object and open
                // writers if we are interrupted!?
                StringBuffer responseString = new StringBuffer();
                responseString.append("<div class=\"google\">" + "\n");
                responseString.append("<div class=\"google_head\"><img src=\"../images/google-logo.png\"/></div>" + "\n");
                for (Iterator i = searchResponse.getResults().iterator(); i.hasNext();) {
                    SearchResultElement element = (SearchResultElement) i.next();
                    responseString.append("<p>\n")
                        .append("<b>").append(element.getTitle()).append("</b>\n")
                        .append("<br/>\n")
                        .append(element.getSummary()).append("\n")
                        .append("<br/>\n");
                    
                        if(element.getClickUrl().length() > 60)
                            element.setClickUrl(trimClickUrl(element.getClickUrl()));

                        responseString.append("<a class=\"url\" href=\"").append(element.getUrl()).append("\">").append(element.getClickUrl()).append("</a>\n")
                        .append("<p>\n");
                }
                responseString.append("</div>\n");

                try {
                    //TODO: must we accuire a lock/synch on the responseObject here? 
                    // write response back to client in one block.
                    myResponseRef.getWriter().write(responseString.toString());
                    myResponseRef.getWriter().flush();

                } catch (NullPointerException ne) {
                    // no response object available
                    System.out.println("No response available");
                } catch (IOException e) {
                    //More serious error, Error when printing
                    e.printStackTrace();
                }

            } finally {

                // clean up all references 
                googleCommand = null;
                connector = null;
                myResponseRef = null;
            }
        }

    }

	
}
