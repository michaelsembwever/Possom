/**
 * 
 */
package no.schibstedsok.front.searchportal.command;

import java.util.ArrayList;

import no.schibstedsok.front.searchportal.connectors.GoogleConnector;
import no.schibstedsok.front.searchportal.response.CommandResponse;
import no.schibstedsok.front.searchportal.response.GoogleSearchResultElement;
import no.schibstedsok.front.searchportal.response.SearchResponseImpl;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;

import org.apache.log4j.Logger;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResult;

/**
 * @author Lars Johansson
 * 
 */
public class GoogleConnectorCommand implements ConnectorCommand {

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
    public GoogleConnectorCommand() {
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
        
        // Create a Google Search object, set our authorization key
        GoogleSearch s = new GoogleSearch();
        s.setKey(GoogleConnector.googleKey);
        s.setMaxResults(getMaxResultsToReturn());
        s.setStartResult(startSearchAt);
        
        // Depending on user input, do search or cache query, then print out
        // result
        try {
            if (getDirective().equalsIgnoreCase("search")) {

                long timer = System.currentTimeMillis();
                s.setQueryString(getQueryString());
                
                GoogleSearchResult r = s.doSearch();
                
                if (log.isDebugEnabled()) {
                    log.debug("Google Search Results for query: " + getQueryString());
                    log.debug("===============================================================");
                    log.debug(r.toString());
                }

                ArrayList list = new ArrayList();
                if(r.getResultElements() != null) {
                    list = new ArrayList(r.getResultElements().length);
                    for (int i = 0; i < r.getResultElements().length; i++) {
                        list.add(new GoogleSearchResultElement(r.getResultElements()[i]));
                    }
                }
                timer = System.currentTimeMillis() - timer;                
                response = new SearchResponseImpl();
                response.setFetchTime(System.currentTimeMillis() - timer);
                response.setDocumentsReturned(list.size());
                response.setConsequtiveSearchStartsAt(r.getEndIndex());
                response.setTotalDocumentsAvailable(r.getEstimatedTotalResultsCount());
                response.setResults(list);

            } else if (getDirective().equalsIgnoreCase("cached")) {
                if(log.isDebugEnabled()){
                    log.debug("Cached page Results for query: " + getQueryString());
                    log.debug("================================================================");
                }
                byte[] cachedBytes = s.doGetCachedPage(getQueryString());
                // Note - this conversion to String should be done with
                // reference to the encoding of the cached page, but we don't do that
                // here.
                String cachedString = new String(cachedBytes);
                log.debug(cachedString);
            } else if (getDirective().equalsIgnoreCase("spell")) {
                String suggestion = s.doSpellingSuggestion(getQueryString());
                if(log.isDebugEnabled()){
                    log.debug("Spelling suggestion: " + getQueryString());
                    log.debug("================================================================");
                    log.debug(suggestion);
                }
            }
        } catch (GoogleSearchFault f) {
            log.error("The call to the Google Web APIs failed:");
            log.error(f.toString());
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


    public int getMaxResultsToReturn() {
        return maxResults;
    }
    


    public void setMaxResultsToReturn(int maxResults) {
        this.maxResults = maxResults;
    }


    public int getStartSearchAt() {
        return startSearchAt;
    }
    

    public void setStartSearchAt(int startSearchAt) {
        this.startSearchAt = startSearchAt;
    }


	public void setConfiguration(SearchConfiguration config) {
		// DO NOTHING For Google
		
	}
    
    

}
