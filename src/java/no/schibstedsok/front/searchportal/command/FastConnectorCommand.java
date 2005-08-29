/**
 *
 */
package no.schibstedsok.front.searchportal.command;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import no.fast.ds.search.*;
import no.schibstedsok.front.searchportal.configuration.FastSearchConfiguration;
import no.schibstedsok.front.searchportal.connectors.FastConnector;
import no.schibstedsok.front.searchportal.response.*;
import no.schibstedsok.front.searchportal.util.SearchConfiguration;
import no.schibstedsok.front.searchportal.util.SearchConstants;
import no.schibstedsok.front.searchportal.util.PagingDisplayHelper;

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
        } catch (MalformedURLException e1) {
            log.fatal("Unable to connect to FAST index");
            throw new RuntimeException("Unable to connect to FAST index." + e1);
        }

        try {

            ISearchParameters params = new SearchParameters();

            setUpSearchParameters(params);
            response.setQuery(getQueryString());
            response.setModifier(configuration.getCategoryModifer());

            Query query = new Query(params);

            IQueryResult queryResult = doSearch(query);

            if (configuration.getCollection().equals("yellow")) {

                if (configuration.getCategoryModifer() != null) {

                    log.debug("Drilling down");

                    INavigation navigation = new Navigation();
                    INavigator navigator = queryResult.getNavigator("ypbransjenavigator");

                    IAdjustmentGroup adjGroup = navigation.add(navigator);
                    IAdjustment adjustment = adjGroup.add(navigator.getModifier(configuration.getCategoryModifer()));

                    IQuery newQuery = navigation.createNavigatedQuery(query);

                     queryResult = doSearch(newQuery);

                    

                }

                INavigator navigator = queryResult.getNavigator("ypbransjenavigator");

                if (navigator != null) {

                Iterator modifiers = navigator.modifiers();
                while (modifiers.hasNext()) {
                    IModifier modifier = (IModifier) modifiers.next();
                    response.addCategoryModifier(modifier);
                }
                }
            }

//			System.out.println(query);

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

            //set spelling suggestion if available
            if (configuration.isSpellcheck()) {
                setSpellingSuggestion(queryResult);
            }

            //set META data
            response.setFetchTime(System.currentTimeMillis() - timer);
            response.setDocumentsReturned(response.getResults().size());
            response.setTotalDocumentsAvailable(queryResult.getDocCount());

            PagingDisplayHelper pager = new PagingDisplayHelper(queryResult.getDocCount());
            pager.setCurrentOffset(configuration.getOffSet());
            response.setPager(pager);

//			if(log.isDebugEnabled())
//				log.debug("Fast execute() command took: " + (System.currentTimeMillis() - timer) + "msec.");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ;
        }

    }

    private IQueryResult doSearch(IQuery query) throws IOException {
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


        params.setParameter(new SearchParameter(BaseParameter.COLLAPSING, true));
        params.setParameter(new SearchParameter(BaseParameter.FILTER, configuration.constructCollectionFilter()));
        params.setParameter(new SearchParameter(BaseParameter.LANGUAGE, configuration.getLanguage()));
        params.setParameter(new SearchParameter(BaseParameter.TYPE, "all"));
        params.setParameter(new SearchParameter(BaseParameter.NAVIGATION_HITS, configuration.getDocsToReturn()));
//        params.setParameter(new SearchParameter(BaseParameter.CLUSTERING, true));
        params.setParameter(new SearchParameter(BaseParameter.NAVIGATION, true));
        params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE, true));
        if(configuration.isSpellcheck()) {
           params.setParameter(new SearchParameter(BaseParameter.SPELL, "suggest"));
        } else {
            params.setParameter(new SearchParameter(BaseParameter.SPELL, "off"));
        }



        if(configuration.getNavigatorString() != null &! "".equals(configuration.getNavigatorString())) {
            params.setParameter(new SearchParameter(BaseParameter.NAVIGATORS, configuration.getNavigatorString()));
        }

        if ("wikipedia".equals(configuration.getCollection())) {
            params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE, false));
        }
        if ("yellow".equals(configuration.getCollection())) {
            params.setParameter(new SearchParameter(BaseParameter.QUERY, getCompositePhoneticQuery("yellowphon", getQueryString())));
            params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "yellowpages"));
        } else if ("white".equals(configuration.getCollection())) {
            params.setParameter(new SearchParameter(BaseParameter.QUERY, getCompositePhoneticQuery("whitephon", getQueryString())));
            params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE, false));
            params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "whitepages"));
        } else {
            params.setParameter(new SearchParameter(BaseParameter.QUERY, getQueryString()));
        }


//		if (log.isDebugEnabled()) {
//			try {
//				log.debug("Filter applied: "	+ params.getParameter(BaseParameter.FILTER));
//				log.debug("Asking for navigators: "	+ params.getParameter(BaseParameter.NAVIGATORS));
//			} catch (NoSuchParameterException e) {
//				// silent, only debug purpose not all params is mandatory.
//			}
//		}

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
		if(configuration.getCollection().equals(SearchConstants.WEBCRAWL_COLLECTION))
            i =  resultsFromAllCollections(queryResult, i, timer);
        else if(configuration.getCollection().equals(SearchConstants.MEDIA_COLLECTION))
            i =  resultsFromMediaCollection(queryResult, i, timer);
        else if(configuration.getCollection().equals(SearchConstants.WIKI_COLLECTION))
            i =  resultsFromWikiCollection(queryResult, i, timer);
        else if(configuration.getCollection().equals(SearchConstants.COMPANIES_COLLECTION))
            i =  resultsFromCompaniesCollection(queryResult, i);
        else if(configuration.getCollection().equals(SearchConstants.PERSONS_COLLECTION))
            i =  resultsFromPersonsCollection(queryResult, i);
        return i;
    }


    private int resultsFromMediaCollection(IQueryResult queryResult, int i, long timer) {
        for (; i <= getMaxResultsToReturn(); i++) {
            try {
                SearchResultElement result = new FastRetrieverSearchResult(queryResult.getDocument(i + configuration.getOffSet()));
                response.addRetreiverResult(result);
            } catch (IndexOutOfBoundsException e) {
                log.debug("Result set exhausted");
            }
        }
        return i;
    }

    private int resultsFromWikiCollection(IQueryResult queryResult, int i, long timer) {
        for (; i <= getMaxResultsToReturn(); i++) {
            try {
                SearchResultElement result = new FastWikiSearchResult(queryResult.getDocument(i + configuration.getOffSet()));
                response.addWikiResult(result);
            } catch (IndexOutOfBoundsException e) {
                log.debug("Result set exhausted");
            }
        }
        return i;
    }

    private int resultsFromPersonsCollection(IQueryResult queryResult, int i) {

        for (; i <= getMaxResultsToReturn(); i++) {
            SearchResultElement result = null;
            try {
                result = new FastPersonsSearchResult(queryResult.getDocument(i + configuration.getOffSet()));
                response.addPersonsResult(result);
            } catch (IndexOutOfBoundsException e) {
                log.debug("Result set exhausted.");
                return i;
            }
        }
        return i;
    }

    private int resultsFromCompaniesCollection(IQueryResult queryResult, int i) {

        for (; i <= getMaxResultsToReturn(); i++) {
            SearchResultElement result = null;
            try {
                result = new FastCompaniesSearchResult(queryResult.getDocument(i + configuration.getOffSet()));
                response.addCompaniesResult(result);
            } catch (IndexOutOfBoundsException e) {
                log.debug("Result set exhausted.");
                return i;
            }
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

    public FastSearchConfiguration getConfiguration() {
//		System.out.println(this.configuration);
        return (FastSearchConfiguration)this.configuration;
    }


    public String getCompositePhoneticQuery(String prefix, String query) {

        String[] tokens = query.split("\\s");

        StringBuffer newQuery = new StringBuffer();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].indexOf(":") == -1) {
                newQuery.append(prefix).append(":");
            }

            newQuery.append(tokens[i]);

            if (i < tokens.length - 1) {
                newQuery.append(" ");
            }
        }

        return newQuery.toString();
    }
}
