/**
 * 
 */
package no.schibstedsok.front.searchportal.filters.overture;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.command.OvertureConnectorCommand;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.connectors.OvertureConnector;
import no.schibstedsok.front.searchportal.connectors.interfaces.Connector;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.response.SearchResultElement;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

/**
 * @author Lars Johansson
 *
 */
public class OvertureSearchConsumer extends SearchConsumer {

	SearchConfiguration config = null;

    /**
     * @param query
     * @param maxWaitTime
     * @param response
     */
    public OvertureSearchConsumer(String query, long maxWaitTime,
            ServletResponse response, int maxResults, int offset) {

		super(response, new FastSearchConfigurationImpl());

		FastSearchConfigurationImpl config = new FastSearchConfigurationImpl();
		config.setQuery(query);
		config.setMaxTime(maxWaitTime);
		config.setDocsToReturn(maxResults);
		config.setOffSet(offset);


        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.filters.SearchConsumer#run()
     */
    public void run() {
        {
            SearchThread w = new SearchThread(config.getQuery(), myResponseRef);
            final Thread searchThread = new Thread(w);

            searchThread.start();

            final Object lock = new Object();
            synchronized (lock) {
                try {
					lock.wait(((FastSearchConfiguration) config).getMaxTime());
//                    System.out.println("Overture SearchConsumer waited " + config.getMaxTime() + " msec");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            searchThread.interrupt();
        }
    }

    /**
     * 
     * Inner class implementing the Overture search
     * 
     * @author Lars Johansson
     *
     */
    public class SearchThread implements Runnable {

        Connector connector = new OvertureConnector();
		OvertureConnectorCommand command = new OvertureConnectorCommand();

        ServletResponse myResponseRef = null;
        private String myQuery = null;

        public SearchThread(String query, ServletResponse response) {
            myQuery = query;
            myResponseRef = response;
        }

        public void run() {
            try {
                command.setDirective("search");
                command.setQueryString(myQuery);

                // connect and execute search!
                connector.execute(command, null);

                CommandResponse searchResponse = command.getResponse();

                // TODO: What happens with the response object and open
                // writers if we are interrupted!?
                
                 StringBuffer responseString = new StringBuffer();
                 responseString.append("<div class=\"overture\">" + "\n");
                 responseString.append("<div class=\"overture_logo\"><img src=\"../images/overture-logo.png\"/></div>" + "\n");
                 responseString.append("<div class=\"overture_head\">Search took:")
                     .append(searchResponse.getFetchTime())
                     .append("millisec.")
                     .append("</div>" + "\n");
                 for (Iterator i = searchResponse.getResults().iterator();i.hasNext();) {
                     
                     SearchResultElement element = (SearchResultElement) i.next();
                     responseString.append("<p>\n")
                     .append("<b>")
                     .append(element.getTitle())
                     .append("</b>")
                     .append("<br/>\n")
                     .append(element.getSummary())
                     .append("<br/><a class=\"url\" href=\"http://").append(element.getClickUrl()).append("\">").append(element.getUrl()).append("</a>\n")
                    .append("<p>\n");
                }
                 responseString.append("</div>\n");

                                    
                 try {
                 //TODO: must we accuire a lock/synch on the responseObject
                 //here?
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
                command = null;
                connector = null;
                myResponseRef = null;
            }
        }

    }

}
