/*
 * Copyright (2005) Schibsted Sok AS
 */
package no.schibstedsok.front.searchportal.command;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.INavigator;
import no.fast.ds.search.IQueryResult;
import no.fast.ds.search.IQueryTransformation;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.Query;
import no.fast.ds.search.SearchEngineException;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchParameters;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.connectors.FastConnector;
import no.schibstedsok.front.searchportal.response.FastSearchResponseImpl;
import no.schibstedsok.front.searchportal.response.FastSearchResult;
import no.schibstedsok.front.searchportal.response.SearchResultElement;
import no.schibstedsok.front.searchportal.util.PagingDisplayHelper;

import org.apache.log4j.Logger;

/**
 * A SensisConnectorCommand.
 * 
 * @author <a href="lars.johansson@conduct.no">Lars Johansson</a>
 * @version $Revision$
 */
public class SensisConnectorCommand extends FastConnectorCommand implements ConnectorCommand {

	/** Logger for this class. */
    private Logger log = Logger.getLogger(this.getClass());


	/**
	 * Create a new SensisConnectorCommand.
	 * 
	 */
	public SensisConnectorCommand() {
		super();
	}

    /*
     * (non-Javadoc)
     * 
     * @see com.schibstedsok.portal.search.connectors.ConnectorCommand#execute()
     */
    public void execute() {

        if ("".equals(getQueryString()))
            return;

        long timer = System.currentTimeMillis(); // timer for search

        FastConnector connector = (FastConnector)FastConnector.getInstance();
		
		FastSearchConfiguration config = super.getConfiguration();
		 
		try {
            engine = connector.factory.createSearchEngine(config.getQRServerURL());
			if(log.isDebugEnabled()){
				log.debug("Created Sensis search-engine for: " + config.getQRServerURL());
			}
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        try {

            ISearchParameters params = new SearchParameters(
                    new SearchParameter(BaseParameter.QUERY, getQueryString()));
			params.setParameter(new SearchParameter(BaseParameter.FILTER, config.constructCollectionFilter()));
            params.setParameter(new SearchParameter(BaseParameter.LANGUAGE, config.getLanguage()));
            params.setParameter(new SearchParameter(BaseParameter.TYPE, "all"));
			params.setParameter(new SearchParameter(BaseParameter.NAVIGATION_HITS, config.getDocsToReturn()));
            params.setParameter(new SearchParameter(BaseParameter.NAVIGATORS, config.getNavigatorString()));
            if(config.isSpellcheck())
               params.setParameter(new SearchParameter(BaseParameter.SPELL, "suggest"));

            //TODO: add more filters for the query
            
            Query query = new Query(params);
            
			long searchTimer = System.currentTimeMillis();
			
            IQueryResult queryResult = null;
			try {
				queryResult = engine.search(query);
				if(log.isDebugEnabled())
					log.debug("Sensis " + config.getCollection() + " offset: " + config.getOffSet() + " search() took: " + (System.currentTimeMillis() - searchTimer) + "msec.");

	            ArrayList results = new ArrayList();
	            response = new FastSearchResponseImpl();
				response.setQuery(getQueryString());

	            if (queryResult.getDocCount() <= getMaxResultsToReturn() + config.getOffSet()) {
	                setMaxResultsToReturn(queryResult.getDocCount());
	            }
				
				//doc counter
				int i = 1;
	            
				i = handleResult(queryResult, results, i);

				setConsequtiveSearch(queryResult, i);
				setSpellingSuggestion(queryResult); 
				
				if(log.isDebugEnabled()){
					Iterator navigators = queryResult.navigators();
					while (navigators.hasNext()) {
						INavigator navigator = (INavigator) navigators.next();
						log.debug("Navigators found :" + navigator.getDisplayName());
					}
				}

	            response.setFetchTime(System.currentTimeMillis() - timer);
	            response.setDocumentsReturned(response.getResults().size());
	            response.setTotalDocumentsAvailable(queryResult.getDocCount());

                PagingDisplayHelper pager = new PagingDisplayHelper(queryResult.getDocCount());
                pager.setCurrentOffset(config.getOffSet());
                response.setPager(pager);

				if(log.isDebugEnabled())
					log.debug("Sensis execute() command took: " + (System.currentTimeMillis() - timer) + "msec.");

			} catch (SearchEngineException e) {
				log.fatal("Fast error when doing search: " + config.getQuery() + " " + e.getMessage());
				response.setSearchErrorMesg("Unable to connect to search index: " + e.getMessage());
			}
		
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	
	/**
	 * 
	 *  Parse the queryResult and handle different type of collection
	 *  results. Adds results to the response 
	 * 
	 * @param queryResult
	 * @param results
	 * @param i
	 * @return
	 */
	private int handleResult(IQueryResult queryResult, ArrayList results, int i) {
		
        long timer = System.currentTimeMillis(); // timer for search

		int j = 0; //counter to keep track of wiki or media results returned
		
		FastSearchConfiguration config = super.getConfiguration();
		
		//iterate over results and populate our search-result object
		for (; i <= getMaxResultsToReturn() + j; i++) { // one-indexed collection
		    
			SearchResultElement result = new FastSearchResult(queryResult.getDocument(i + config.getOffSet()));
		    response.addResult(result);
			
		}

//		if(log.isDebugEnabled())
//			log.debug("Sensis handleResult() took: " + (System.currentTimeMillis() - timer) + "msec.");
		
		return i;
	}

	private void setConsequtiveSearch(IQueryResult queryResult, int i) {
		// Set a pointer to the next document
		if(queryResult.getDocCount() > getMaxResultsToReturn() ) {
		    response.setConsequtiveSearchStartsAt(i);				
		}
	}

	private void setSpellingSuggestion(IQueryResult queryResult) {
//		if (!queryResult.getQueryTransformations(false).getSuggestions().isEmpty()) {
			String query_suggestions = "";
			for (Iterator iter = queryResult.getQueryTransformations(false)
					.getAllQueryTransformations().iterator(); iter.hasNext();) {
				IQueryTransformation xform = (IQueryTransformation) iter.next();
				if (xform.getMessage() != null && xform.getMessageID() == 0 && xform.getQuery().toUpperCase().indexOf(getQueryString().toUpperCase()) == -1) {
					// foundValidSuggestion = true;
					response.addSpellingSuggestion(xform.getQuery());
				}
			}
//		}
	}

//	public String getQueryString(){
//		return getConfiguration().getQuery();
//	}
}
