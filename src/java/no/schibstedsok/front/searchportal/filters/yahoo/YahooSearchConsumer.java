/**
 * 
 */
package no.schibstedsok.front.searchportal.filters.yahoo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;

import javax.servlet.ServletResponse;

import no.schibstedsok.front.searchportal.command.YahooConnectorCommand;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfigurationImpl;
import no.schibstedsok.front.searchportal.filters.SearchConsumer;
import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.response.SearchResultElement;

/**
 * @author Lars Johansson
 *
 */
public class YahooSearchConsumer extends SearchConsumer {


    private FastSearchConfiguration config;

	/**
     * @param query
     * @param maxWaitTime
     * @param response
     */
    public YahooSearchConsumer(String query, long maxWaitTime,
            ServletResponse response, int maxResults, int offset) {

		super(response, new FastSearchConfigurationImpl());
		
		config = new FastSearchConfigurationImpl();
		config.setQuery(query);
		config.setMaxTime(maxWaitTime);
		config.setDocsToReturn(maxResults);
		config.setOffSet(offset);
    }

    /* (non-Javadoc)
     * @see com.schibstedsok.portal.search.filters.SearchConsumer#run()
     */
    public void run() {
        {
            SearchThread w = new SearchThread(config.getQuery(), myResponseRef);
            final Thread yahooThread = new Thread(w);

            yahooThread.start();

            final Object lock = new Object();
            synchronized (lock) {
                try {
                    lock.wait(config.getMaxTime());
                    System.out.println("SearchConsumer waited " + config.getMaxTime()
                            + " msec");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            yahooThread.interrupt();
        }
    }

    /**
     * 
     * Inner class implementing the Yahoo search
     * 
     * 
     * @author Lars Johansson
     *
     */
    public class SearchThread implements Runnable {

        YahooConnectorCommand yahooCommand = new YahooConnectorCommand();

        ServletResponse myResponseRef = null;

        private String myQuery = null;

        public SearchThread(String query, ServletResponse response) {
            myQuery = query;
            myResponseRef = response;
        }

        public void run() {
            try {
                yahooCommand.setDirective("search");
                yahooCommand.setQueryString(myQuery);

                // connect and execute search!
                yahooCommand.execute();

                CommandResponse searchResponse = yahooCommand.getResponse();

                // TODO: What happens with the response object and open
                // writers if we are interrupted!?
                StringBuffer responseString = new StringBuffer();
                responseString.append("<div class=\"yahoo\">" + "\n");
                responseString.append("<div class=\"yahoo_logo\"><img src=\"../images/yahoo-logo.png\"/></div>" + "\n");
                responseString.append("<div class=\"yahoo_head\">Search took:")
                    .append(searchResponse.getFetchTime())
                    .append("millisec.")
                    .append(" Returning ").append(searchResponse.getDocumentsReturned())
                    .append(" of ").append(searchResponse.getTotalDocumentsAvailable())
                    .append(" total documents available...")
                    .append("</div>" + "\n");

                for (Iterator i = searchResponse.getResults().iterator(); i.hasNext();) {
                    SearchResultElement element = (SearchResultElement) i.next();
                    responseString.append("<p>\n")
                        .append("<b>").append(element.getTitle()).append("</b>\n")
                        .append("<br/>\n")
                        .append(element.getSummary()).append("\n")
                        .append("<br/>\n");
                        if(element.getUrl().equals(element.getClickUrl()))
                            try {
                                element.setClickUrl(URLDecoder.decode(element.getClickUrl().substring(element.getClickUrl().lastIndexOf("-http") + 1), "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
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
                yahooCommand = null;
                myResponseRef = null;
            }
        }

    }

}
