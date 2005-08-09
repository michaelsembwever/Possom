/**
 *
 */
package no.schibstedsok.front.searchportal.command;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.IFastSearchEngine;
import no.fast.ds.search.IModifier;
import no.fast.ds.search.INavigator;
import no.fast.ds.search.IQueryResult;
import no.fast.ds.search.IQueryTransformation;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.NoSuchParameterException;
import no.fast.ds.search.Query;
import no.fast.ds.search.SearchEngineException;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchParameters;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.connectors.FastConnector;
import no.schibstedsok.front.searchportal.response.*;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;

import org.apache.log4j.Logger;

/**
 * @author Lars Johansson
 *
 */
public class FastConnectorCommand implements ConnectorCommand {

    /** Logger for this class. */
    private Logger log = Logger.getLogger(this.getClass());

    protected FastSearchResponseImpl response;

    protected String queryString;

	protected String directive;

    IFastSearchEngine engine = null;

	protected int maxResults;

//	protected int startSearchAt = 1; // default starting postion in search

	private FastSearchConfiguration configuration;


    /**
     *
     */
    public FastConnectorCommand() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.schibstedsok.portal.search.connectors.ConnectorCommand#execute()
     */
    public void execute() {

		response = new FastSearchResponseImpl();

        if ("".equals(getQueryString()))
            return;

        long timer = System.currentTimeMillis(); // timer for search

        FastConnector connector = (FastConnector)FastConnector.getInstance();

		try {
            engine = connector.factory.createSearchEngine(configuration.getQRServerURL());
			if(log.isDebugEnabled()){
				log.debug("Created Fast search-engine for: " + configuration.getQRServerURL());
			}
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
			log.fatal("Unable to connect to FAST index");
			throw new RuntimeException("Unable to connect to FAST index." + e1);
        }

        try {

		    ISearchParameters params = new SearchParameters();

			setUpSearchParameters(params);
			response.setQuery(getQueryString());

            Query query = new Query(params);

			IQueryResult queryResult = doSearch(query);

			//abort search on error
			if(queryResult == null)
				return;

            ArrayList results = new ArrayList();

			//check if there was less documents than our maximum to return
            if (queryResult.getDocCount() <= getMaxResultsToReturn() + configuration.getOffSet()) {
                setMaxResultsToReturn(queryResult.getDocCount());
            }

			//set a counter for how many documents exists in the various collection
			if(true)	//TODO: look if we're doing a navigator search
				extractDocumentsInCollections(connector, queryResult);

			//doc counter
			int i = 1;

			//populate results from search
			i = handleResult(queryResult, results, i);

			//do the back, current and next set population if there was documents returned
			if(getMaxResultsToReturn() > 0)
				handleNavigationInSets(queryResult, i);

			//set spelling suggestion if available
			setSpellingSuggestion(queryResult);

            //set META data
			response.setFetchTime(System.currentTimeMillis() - timer);
            response.setDocumentsReturned(response.getResults().size());
            response.setTotalDocumentsAvailable(queryResult.getDocCount());
			response.setConsequtiveSearchStartsAt(i + configuration.getOffSet());

//			if(log.isDebugEnabled())
//				log.debug("Fast execute() command took: " + (System.currentTimeMillis() - timer) + "msec.");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
			;
        }

    }

	private IQueryResult doSearch(Query query) throws IOException {
		long searchTimer = System.currentTimeMillis();
		IQueryResult queryResult = null;
		try {
			queryResult = engine.search(query);
		} catch (SearchEngineException e) {
			log.fatal("Fast error when doing search: \"" + configuration.getQuery() + "\". " + e.getMessage());
			response.setSearchErrorMesg("Unable to connect to search index: " + e.getMessage());
		}

		if(log.isDebugEnabled())
			log.debug("Fast " + configuration.getCollection() +  " offset: " + configuration.getOffSet() + " search() took: " + (System.currentTimeMillis() - searchTimer) + "msec.");

		return queryResult;
	}

	private void setUpSearchParameters(ISearchParameters params) {

		params.setParameter(new SearchParameter(BaseParameter.QUERY, getQueryString()));
		params.setParameter(new SearchParameter(BaseParameter.FILTER, configuration.constructCollectionFilter()));
		params.setParameter(new SearchParameter(BaseParameter.LANGUAGE, configuration.getLanguage()));
		params.setParameter(new SearchParameter(BaseParameter.TYPE, "all"));
		params.setParameter(new SearchParameter(BaseParameter.NAVIGATION_HITS, configuration.getDocsToReturn()));
		params.setParameter(new SearchParameter(BaseParameter.CLUSTERING, true));
		params.setParameter(new SearchParameter(BaseParameter.NAVIGATION, true));
		params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE, true));
		if(configuration.isSpellcheck())
		   params.setParameter(new SearchParameter(BaseParameter.SPELL, "suggest"));

		if(configuration.getNavigatorString() != null &! "".equals(configuration.getNavigatorString())) {
			params.setParameter(new SearchParameter(BaseParameter.NAVIGATORS, configuration.getNavigatorString()));
		}

		if (log.isDebugEnabled()) {
			try {
				log.debug("Filter applied: "	+ params.getParameter(BaseParameter.FILTER));
				log.debug("Asking for navigators: "	+ params.getParameter(BaseParameter.NAVIGATORS));
			} catch (NoSuchParameterException e) {
				// silent, only debug purpose not all params is mandatory.
			}
		}

//		System.out.println(params);
	}

	private void extractDocumentsInCollections(FastConnector connector, IQueryResult queryResult) {

		Iterator navigators = queryResult.navigators();
		while (navigators.hasNext()) {
			INavigator navigator = (INavigator) navigators.next();
			Iterator contentSources = navigator.modifiers();
			while (contentSources.hasNext()) {
				IModifier source = (IModifier) contentSources.next();
				if(source.getName().equals(SearchConstants.WEBCRAWL_COLLECTION_NAVIGATOR)) {
					response.setWebCrawlDocumentsInIndex(source.getCount());
				} else if(source.getName().equals(SearchConstants.MEDIA_COLLECTION_NAVIGATOR)) {
					response.setMediaDocumentsInIndex(source.getCount());
				} else if(source.getName().equals(SearchConstants.WIKI_COLLECTION_NAVIGATOR)) {
					response.setWikiDocumentsInIndex(source.getCount());
				} else if(source.getName().equals(SearchConstants.COMPANIES_COLLECTION_NAVIGATOR)) {
                    response.setCompaniesDocumentsInIndex(source.getCount());
                }
			}
		}
	}

	private void handleNavigationInSets(IQueryResult queryResult, int i) {
		//keep track of the back and forward position

		setConsequtiveSearch(queryResult, i + configuration.getOffSet() - 1);
		response.setCurrentPostionInSet(response.getConsequtiveSearchStartsAt()/getMaxResultsToReturn());
		int currentPosition = response.getCurrentPostionInSet();
		int[] previousSet = new int[response.getCurrentPostionInSet()];
		for(int j = currentPosition % getMaxResultsToReturn(); j > 1; j--){
			previousSet[j-1] = currentPosition - j + 1;
//			System.out.print(previousSet[j-1] + " ");
		}
//		System.out.print("'" + currentPosition + "'");
		int[] nextSet = new int[10 - currentPosition % 10];
		for(int j= 1 ; j < 10 - currentPosition % 10 + 1;j++) {
			nextSet[j-1] = currentPosition + j;
//			System.out.print(" " + nextSet[j-1]);
		}
//		System.out.println();
		response.setPreviousSet(previousSet);
		response.setNextSet(nextSet);
	}

	/**
	 *
	 *  Parse the queryResult and handle different type of collection
	 *  results. Adds results to the response
	 *
	 * @param queryResult
	 * @param results
	 * @param i
	 * @return <code>int</code> The current position in resultset.
	 */
	private int handleResult(IQueryResult queryResult, ArrayList results, int i) {

		//TODO: refactor this to be more generic!

		long timer = System.currentTimeMillis(); // timer
		if(configuration.getCollection().equals(SearchConstants.DEFAULTCOLLECTION))
			i =  resultsFromAllCollections(queryResult, i, timer);
		else if(configuration.getCollection().equals(SearchConstants.MEDIA_COLLECTION))
			i =  resultsFromMediaCollection(queryResult, i, timer);
        else if(configuration.getCollection().equals(SearchConstants.WIKI_COLLECTION))
            i =  resultsFromAllCollections(queryResult, i, timer);
        else if(configuration.getCollection().equals(SearchConstants.COMPANIES_COLLECTION))
            i =  resultsFromCompaniesCollection(queryResult, i);
		return i;
	}


	private int resultsFromMediaCollection(IQueryResult queryResult, int i, long timer) {
        for (; i <= getMaxResultsToReturn(); i++) {
            SearchResultElement result = new FastSearchResult(queryResult.getDocument(i + configuration.getOffSet()));
            response.addRetreiverResult(result);
        }
        return i;
	}

    private int resultsFromCompaniesCollection(IQueryResult queryResult, int i) {
        for (; i <= getMaxResultsToReturn(); i++) {
            SearchResultElement result = new FastCompaniesSearchResult(queryResult.getDocument(i + configuration.getOffSet()));
            response.addCompaniesResult(result);
        }
        return i;
    }

    private int resultsFromAllCollections(IQueryResult queryResult, int i, long timer) {

		int wiki = 0;
		int media = 0; //counter to keep track of wiki or media results returned
		//iterate over results and populate our search-result object
		for (; i <= getMaxResultsToReturn() + wiki + media; i++) { // one-indexed collection

			if(i + configuration.getOffSet()> queryResult.getDocCount())
				break;

			SearchResultElement result = new FastSearchResult(queryResult.getDocument(i + configuration.getOffSet()));

			/**
			 *  Check wich collection the result is from
			 *  if we get results from wiki continue until we
			 *  return minimum number of webrawl results.
			 */
			//wiki
            if(queryResult.getDocument(i).getSummaryField("collection").getSummary().equals(SearchConstants.WIKI_COLLECTION)){
                wiki++;	//if we get a wiki or media we must adjust the counter for document to return for national index
                response.addWikiResult(result);
            }
			//retriever
		    else if(queryResult.getDocument(i).getSummaryField("collection").getSummary().equals(SearchConstants.MEDIA_COLLECTION)){
				media++;	//if we get a wiki or media we must adjust the counter for document to return for national index
				if(media > 5)
					continue;
				else
					response.addRetreiverResult(result);
		    }       //webrawl
		    else {
				response.addResult(result);
		    }
		}
//		if(log.isDebugEnabled()){
//			log.debug("HandleResult() took: " + (System.currentTimeMillis() - timer) + "msec.");
//		}
		return i - wiki - media;	//concurrent search should not take wiki or media into account in resultset..
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
					response.addSpellingSuggestion(xform.getQuery().replaceAll("\"", ""));
				}
			}
//		}
//		if(log.isDebugEnabled())
//			log.debug(response.getSpellingSuggestions());
	}

    /*
     * (non-Javadoc)
     *
     * @see com.schibstedsok.portal.search.connectors.ConnectorCommand#getResponse()
     */
    public CommandResponse getResponse() {
        return this.response;
    }

//    public String getDirective() {
//        return directive;
//    }
//
//    public void setDirective(String directive) {
//        this.directive = directive;
//    }

    public String getQueryString() {
        return configuration.getQuery();
    }

    public void setMaxResultsToReturn(int count) {
        this.configuration.setDocsToReturn(count);
    }

    public int getMaxResultsToReturn() {
        return configuration.getDocsToReturn();
    }

    public int getStartSearchAt() {
        return configuration.getOffSet();
    }

	public void setConfiguration(SearchConfiguration config) {
		this.configuration = (FastSearchConfiguration)config;
	}

	/**
	 * Used from Sensis subclass
	 *
	 * @return
	 */
	public FastSearchConfiguration getConfiguration() {
//		System.out.println(this.configuration);
		return (FastSearchConfiguration)this.configuration;
	}

}
