/**
 * 
 */
package no.schibstedsok.front.searchportal.command;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.response.SearchResponseImpl;
import no.schibstedsok.front.searchportal.response.YahooSearchResultElement;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

import org.apache.log4j.Logger;

import com.yahoo.search.SearchClient;
import com.yahoo.search.SearchException;
import com.yahoo.search.SpellingSuggestionRequest;
import com.yahoo.search.SpellingSuggestionResults;
import com.yahoo.search.WebSearchRequest;
import com.yahoo.search.WebSearchResult;
import com.yahoo.search.WebSearchResults;

/**
 * @author Lars Johansson
 * 
 */
public class YahooConnectorCommand implements ConnectorCommand {

    /** Logger for this class. */
    private Logger log = Logger.getLogger(this.getClass());
    
    private CommandResponse response;
    private String queryString;
    private String directive;

    private int maxResults;
    
    private int startSearchAt = 1;      // default starting postion in search
	
	private final static SearchConfiguration configuration = null;


    /**
     * 
     */
    public YahooConnectorCommand() {
        super();
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.schibstedsok.portal.search.connectors.ConnectorCommand#execute()
     */
    public void execute() {
        
        if("".equals(getQueryString()))
            return;
        
        long timer = System.currentTimeMillis();    //time the search   
        
        // Create a Yahoo Search object, set our authorization key
        SearchClient client = new SearchClient("AID_lkadj21l12ej");
        
        // Depending on user input, do search or cache query, then print out
        // result
        try {
            if (getDirective().equalsIgnoreCase("search")) {

                WebSearchRequest request = new WebSearchRequest(getQueryString());
                request.setResults(maxResults);     // set the maximum documents we will fetch
                request.setStart(new BigInteger(getStartSearchAt() + ""));
                
                WebSearchResults results = client.webSearch(request);
        
                
                if (log.isDebugEnabled()) {
                    log.debug("Yahoo Search Results for query: " + getQueryString());
                    log.debug("===============================================================");
                    log.debug(results.toString());
                }

                ArrayList list = new ArrayList();
				int i = 0;
                for (; i < results.listResults().length; i++) {
                    WebSearchResult result = results.listResults()[i];
                    list.add(new YahooSearchResultElement(result));
                }

                response = new SearchResponseImpl();
                response.setFetchTime(System.currentTimeMillis() - timer);
                response.setDocumentsReturned(i);
                response.setTotalDocumentsAvailable(results.getTotalResultsAvailable().intValue());
                response.setConsequtiveSearchStartsAt(i + 1);
                response.setResults(list);

            } else if (getDirective().equalsIgnoreCase("cached")) {
              log.info("Cached results is not implemented for Yahoo");
            } else if (getDirective().equalsIgnoreCase("spell")) {
                
                SpellingSuggestionRequest request = new SpellingSuggestionRequest(getQueryString());
                SpellingSuggestionResults results = client.spellingSuggestion(request);
                
                if(log.isDebugEnabled()){
                    log.debug("Spelling suggestion: " + getQueryString());
                    log.debug("================================================================");
                    log.debug(results.getSuggestion());
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SearchException e) {
            log.error("Yahoo error!" + e);
            e.printStackTrace();
        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.schibstedsok.portal.search.connectors.ConnectorCommand#getResponse()
     */
    public CommandResponse getResponse() {
        return this.response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.schibstedsok.portal.search.connectors.ConnectorCommand#setResponse(com.schibstedsok.portal.search.connectors.ConnectorResponse)
     */
    public void setResponse(CommandResponse response) {
        this.response = response;
    }


    public String getDirective() {
        return directive;
    }

    public void setDirective(String directive) {
        this.directive = directive;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String query) {
        this.queryString = query;
    }


    public void setMaxResultsToReturn(int count) {
        this.maxResults = count;
    }


    public int getMaxResultsToReturn() {
        // TODO Auto-generated method stub
        return maxResults;
    }


    public int getStartSearchAt() {
        return startSearchAt;
    }
    


    public void setStartSearchAt(int startSearchAt) {
        this.startSearchAt = startSearchAt;
    }


	public void setConfiguration(SearchConfiguration config) {
		// DO NOTHING for Yahoo
		
	}
    

}
