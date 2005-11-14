/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.command;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.ConfigurationException;
import no.fast.ds.search.FastSearchEngineFactory;
import no.fast.ds.search.IDocumentSummary;
import no.fast.ds.search.IDocumentSummaryField;
import no.fast.ds.search.IFastSearchEngine;
import no.fast.ds.search.IFastSearchEngineFactory;
import no.fast.ds.search.IModifier;
import no.fast.ds.search.INavigator;
import no.fast.ds.search.IQuery;
import no.fast.ds.search.IQueryResult;
import no.fast.ds.search.IQueryTransformation;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.NoSuchParameterException;
import no.fast.ds.search.Query;
import no.fast.ds.search.SearchEngineException;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchParameters;
import no.schibstedsok.front.searchportal.InfrastructureException;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.KeywordCluster;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.SearchResultItem;
import no.schibstedsok.front.searchportal.spell.SpellingSuggestion;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastSearchCommand extends AbstractSearchCommand implements SearchCommand {
    private static Log log = LogFactory.getLog(FastSearchCommand.class);
    private static HashMap searchEngines = new HashMap();
    FastConfiguration fastConfiguration;
    private static IFastSearchEngineFactory engineFactory;

    private Map navigatedTo = new HashMap();
    private Map navigatedValues = new HashMap();

    static {
        try {
            engineFactory = FastSearchEngineFactory.newInstance();
        } catch (ConfigurationException e) {
            throw new InfrastructureException(e);
        }
    }

    public FastSearchCommand(final RunningQuery query,
                             final FastConfiguration config,
                             Map parameters) {
        super(query, config, parameters);
        this.fastConfiguration = config;
    }

    public SearchResult execute() {

        try {
            if (fastConfiguration.getNavigators() != null) {
                for (Iterator iterator = fastConfiguration.getNavigators().keySet().iterator(); iterator.hasNext();) {
                    String navigatorKey = (String) iterator.next();

                    if (getParameters().containsKey("nav_" + navigatorKey)) {
                        String navigatedTo[] = (String[]) getParameters().get("nav_" + navigatorKey);
                        addNavigatedTo(navigatorKey, navigatedTo[0]);
                    } else {
                        addNavigatedTo(navigatorKey, null);
                    }
                }
            }

            long start = System.currentTimeMillis();
            IFastSearchEngine engine = getSearchEngine();
            IQuery fastQuery = createQuery();


            if (log.isDebugEnabled()) {
                log.debug(configuration.getName() + " call: " + fastQuery);
            }

            IQueryResult result = null;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("engine.search()");
                    log.debug("execute().configuration: QueryServerURL=" + fastConfiguration.getQueryServerURL());
                    log.debug("execute().configuration: Collections=" + fastConfiguration.getCollections());
                    log.debug("execute().configuration: Name=" + fastConfiguration.getName());
                    log.debug("execute().configuration: Query=" + fastQuery.getQueryString());
                    log.debug("execute().configuration: Filter=" + fastConfiguration.getCollectionFilterString());

                }

                result = engine.search(fastQuery);

                if (log.isDebugEnabled()) {
                    log.debug("Hits is " + configuration.getName() + ":" + result.getDocCount());
                }
            } catch (Exception fastException) {
                log.error("An error occured in FAST code " + fastException.getClass().getName());
                log.error("Configuration is " + configuration.getName());
                return new FastSearchResult(this);
            }


            if (log.isDebugEnabled()) {
                log.debug("QUERY DUMPT: " + fastQuery);
                String filter = null;
                String query = null;

                try {
                    filter = fastQuery.getStringParameter("filter");
                    query = fastQuery.getStringParameter(BaseParameter.QUERY);
                } catch (NoSuchParameterException e) {

                }
                log.debug("execute:  Filter: " + filter
                        + " , query=" + query
                        + ", doc.count= "
                        + result.getDocCount());
            }
            FastSearchResult searchResult = collectResults(result);

            if (fastConfiguration.isSpellcheckEnabled()) {
                collectSpellingSuggestions(result, searchResult);
            }

            if (fastConfiguration.isKeywordClusteringEnabled()) {
                collectKeywordClusters(result, searchResult);
            }

            if (fastConfiguration.getNavigators() != null) {
                collectModifiers(result, searchResult);
            }

            long stop = System.currentTimeMillis();

            if (log.isDebugEnabled()) {
                log.debug(configuration.getName() + " Retrieved all wanted results in " + (stop - start) + "ms");
            }

            return searchResult;
        } catch (ConfigurationException e) {
            log.error("execute", e);
            throw new InfrastructureException(e);
        } catch (MalformedURLException e) {
            log.error("execute", e);
            throw new InfrastructureException(e);
        } catch (SearchEngineException e) {
            log.error("execute", e);
            throw new InfrastructureException(e);
        }
    }

    private void collectKeywordClusters(IQueryResult result, FastSearchResult searchResult) throws SearchEngineException {
        Iterator i = result.documents();

        int clusterIndex = 0;

        while (clusterIndex++ < 20 && i.hasNext()) {
            IDocumentSummary summary = (IDocumentSummary) i.next();
            IDocumentSummaryField vectorField = summary.getSummaryField("docvector");

            if (vectorField != null) {

                String vector = vectorField.getSummary();

                if (vector != null) {
                    String[] concepts = vector.split("]");
                    for (int idx = 0; idx < concepts.length; idx++) {
                        String[] cpair = concepts[idx].split(",");
                        if (cpair.length > 1) {
                            String concept = cpair[0].substring(1, cpair[0].length());
                            String score = cpair[1];
                            if (concept.split(" ").length >= 2 && concept.length() < 100) {
                                KeywordCluster cluster = new KeywordCluster(concept, new Float(score));
                                searchResult.addKeywordCluster(cluster);
                            }
                        }
                    }
                }
            }
        }
    }

    private void collectSpellingSuggestions(IQueryResult result, FastSearchResult searchResult) {
        if (result.getQueryTransformations(false).getSuggestions().size() > 0) {
            for (Iterator iterator = result.getQueryTransformations(false).getAllQueryTransformations().iterator(); iterator.hasNext();) {
                IQueryTransformation transformation = (IQueryTransformation) iterator.next();

                if (transformation.getName().equals("FastQT_SpellCheck") && transformation.getAction().equals("nop")) {
                    String custom = transformation.getCustom();
                    SpellingSuggestion suggestion = createSpellingSuggestion(custom);
                    searchResult.addSpellingSuggestion(suggestion);
                }
            }
        }

        if (getQuery().getQueryString().equals("42")) {
            SpellingSuggestion egg = new SpellingSuggestion("42", "Meningen med livet", 1000);
            searchResult.addSpellingSuggestion(egg);
        }

        if (getQuery().getQueryString().equalsIgnoreCase("meningen med livet")) {
            SpellingSuggestion egg = new SpellingSuggestion("meningen med livet", "42", 1000);
            searchResult.addSpellingSuggestion(egg);
        }

        if (getQuery().getQueryString().equals("yelo")) {
            SpellingSuggestion suggestion = new SpellingSuggestion("yelo", "sesam", 1000);

            searchResult.addSpellingSuggestion(suggestion);
        }
    }

    private SpellingSuggestion createSpellingSuggestion(String custom) {
        int suggestionIndex = custom.indexOf("->");
        int qualityIndex = custom.indexOf("Quality:");

        if (log.isDebugEnabled()) {
            log.debug("Custom is " + custom);
        }

        String orig = custom.substring(0, suggestionIndex);
        String string = custom.substring(suggestionIndex + 2, qualityIndex - 2);
        String quality = custom.substring(qualityIndex + 9, qualityIndex + 12);

        return new SpellingSuggestion(orig, string, Integer.parseInt(quality));
    }

    private FastSearchResult collectResults(IQueryResult result) {

        if (log.isDebugEnabled()) {
            log.debug(configuration.getName() + " Collecting results. There are " + result.getDocCount());
            log.debug(configuration.getName() + " Number of results to collect: " + configuration.getResultsToReturn());
        }

        FastSearchResult searchResult = new FastSearchResult(this);
        int cnt = getCurrentOffset(0);
        int maxIndex = Math.min(cnt + configuration.getResultsToReturn(), result.getDocCount());
        searchResult.setHitCount(result.getDocCount());

        for (int i = cnt; i < maxIndex; i++) {
            IDocumentSummary document = result.getDocument(i + 1);
            //catch nullpointerException because of unaccurate doccount
            try {
                SearchResultItem item = createResultItem(document);
                searchResult.addResult(item);
            } catch (NullPointerException e) {
                if (log.isDebugEnabled()) log.debug("Error finding document " + e);
                return searchResult;
            }
        }
        return searchResult;
    }


    private SearchResultItem createResultItem(IDocumentSummary document) {
        SearchResultItem item = new BasicSearchResultItem();

        if (configuration.getResultFields() != null) {

            for (Iterator iterator = configuration.getResultFields().iterator(); iterator.hasNext();) {
                String field = (String) iterator.next();
                String name = field;
                String alias = field;
                String aliasSplit[] = field.split("AS");

                if (aliasSplit.length == 2) {
                    name = aliasSplit[0].trim();
                    alias = aliasSplit[1].trim();
                }
                IDocumentSummaryField summary = document.getSummaryField(name);

                if (summary != null) {
                    item.addField(alias, summary.getSummary());
                } else {
                }
            }
        }
        return item;
    }

    protected IFastSearchEngine getSearchEngine() throws ConfigurationException, MalformedURLException {
        if (!searchEngines.containsKey(fastConfiguration.getQueryServerURL())) {
            IFastSearchEngine engine = engineFactory.createSearchEngine(fastConfiguration.getQueryServerURL());
            searchEngines.put(fastConfiguration.getQueryServerURL(), engine);
        }
        return (IFastSearchEngine) searchEngines.get(fastConfiguration.getQueryServerURL());
    }

    private IQuery createQuery() {
        ISearchParameters params = new SearchParameters();
        params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE, fastConfiguration.isLemmatizeEnabled()));


        if (fastConfiguration.isSpellcheckEnabled()) {
            params.setParameter(new SearchParameter(BaseParameter.SPELL, "suggest"));
            params.setParameter(new SearchParameter("qtf_spellcheck:addconsidered", "1"));
            params.setParameter(new SearchParameter("qtf_spellcheck:consideredverbose", "1"));
        }

        String kwString = "";
        String queryString = getTransformedQuery();

        if (fastConfiguration.isKeywordClusteringEnabled()) {
            if (getParameters().containsKey("kw")) {
                kwString = StringUtils.join((String[]) getParameters().get("kw"), " ");
            }

            if (!kwString.equals("")) {
                queryString += " " + kwString;
            }
        }
        // TODO: This is a little bit messy
        // Set filter, the filtertype may be adv
        StringBuffer filter = new StringBuffer(fastConfiguration.getCollectionFilterString());

        if (!fastConfiguration.isIgnoreNavigationEnabled() && fastConfiguration.getNavigators() != null) {
            Collection navStrings = createNavigationFilterStrings();
            filter.append(" ");
            filter.append(" ").append(StringUtils.join(navStrings.iterator(), " "));
        }

        String site = getDynamicParams(getParameters(), "site", null);

        if (site != null) {
            filter.append(" +site:" + site);
        }

        if (fastConfiguration.getOffensiveScoreLimit() > 0) {
            filter.append(" ").append("+ocfscore:<").append(fastConfiguration.getOffensiveScoreLimit());
        }

        if (fastConfiguration.getSpamScoreLimit() > 0) {
            filter.append(" ").append("+spamscore:<").append(fastConfiguration.getSpamScoreLimit());
        }

        // Init dynamic filters
        String dynamicLanguage = getDynamicParams(getParameters(), "language", "");
        String dynamicFilterType = getDynamicParams(getParameters(), "filtertype", "any");
        String dynamicType = getDynamicParams(getParameters(), "type", "all");
        String superFilter = super.getFilter();

        if (superFilter == null) {
            superFilter = "";
        }

        if (log.isDebugEnabled()) {
            log.debug("createQuery: superFilter=" + superFilter);
        }
        params.setParameter(new SearchParameter("filtertype", dynamicFilterType));

        params.setParameter(new SearchParameter(BaseParameter.TYPE, dynamicType));

        params.setParameter(new SearchParameter(BaseParameter.FILTER,
                filter.toString() + " " + dynamicLanguage + " " + superFilter));

        if (fastConfiguration.getQtPipeline() != null) {
            params.setParameter(new SearchParameter(BaseParameter.QTPIPELINE,
                    fastConfiguration.getQtPipeline()));
        }
        params.setParameter(new SearchParameter(BaseParameter.QUERY, queryString));
        params.setParameter(new SearchParameter(BaseParameter.COLLAPSING, fastConfiguration.isCollapsingEnabled()));

        params.setParameter(new SearchParameter(BaseParameter.LANGUAGE, "no"));

        if (fastConfiguration.getNavigators() != null && fastConfiguration.getNavigators().size() > 0) {
            params.setParameter(new SearchParameter(BaseParameter.NAVIGATION, true));
        }

        params.setParameter(new SearchParameter("hits", fastConfiguration.getResultsToReturn()));
        params.setParameter(new SearchParameter(BaseParameter.CLUSTERING, fastConfiguration.isClusteringEnabled()));

        if (fastConfiguration.getResultView() != null) {
            params.setParameter(new SearchParameter(BaseParameter.RESULT_VIEW, fastConfiguration.getResultView()));
        }

        if (fastConfiguration.getSortBy() != null) {
            params.setParameter(new SearchParameter(BaseParameter.SORT_BY, fastConfiguration.getSortBy()));
        }

        // TODO: Refactor
        if (getParameters().containsKey("userSortBy")) {

            String sortBy[] = (String[]) getParameters().get("userSortBy");
            if (log.isDebugEnabled()) {
                log.debug("createQuery: SortBY " + sortBy[0]);
            }
            if ("datetime".equals(sortBy[0])) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "docdatetime+standard"));
            }
        }

        params.setParameter(new SearchParameter(BaseParameter.NAVIGATORS, getNavigatorsString()));

        IQuery query = new Query(params);

        if (log.isDebugEnabled()) {
            log.debug("Constructed query: " + query);
        }

        return query;
    }

    private String getDynamicParams(Map map, String key, String defaultValue) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getDynamicParams(): key=" + key +
                    ", defaultValue=" + defaultValue);
        }
        String value = null;

        Object o = map.get(key);
        if (o == null) {
            value = defaultValue;
        } else if (o instanceof String[]) {
            value = ((String[]) o)[0];
        } else if (o instanceof String) {
            value = (String) o;
        } else {
            throw new IllegalArgumentException("Unkown param instance " + o);
        }

        if (value == null || "null".equals(value) || "".equals(value)) {
            value = defaultValue;
        }
        if (log.isDebugEnabled()) {
            log.debug("getDynamicParams: Return " + defaultValue);
        }
        return value;
    }

    public Collection createNavigationFilterStrings() {
        Collection filterStrings = new ArrayList();

        for (Iterator iterator = navigatedValues.keySet().iterator(); iterator.hasNext();) {
            String field = (String) iterator.next();

            String modifiers[] = (String[]) navigatedValues.get(field);


            for (int i = 0; i < modifiers.length; i++) {
                filterStrings.add("+" + field + ":\"" + modifiers[i] + "\"");
            }
        }

        return filterStrings;
    }

    private String getNavigatorsString() {
        if (fastConfiguration.getNavigators() != null) {

            Collection allFlattened = new ArrayList();

            for (Iterator iterator = fastConfiguration.getNavigators().values().iterator(); iterator.hasNext();) {
                FastNavigator navigator = (FastNavigator) iterator.next();
                allFlattened.addAll(flattenNavigators(new ArrayList(), navigator));
            }

            return StringUtils.join(allFlattened.iterator(), ',');
        } else {
            return "";
        }
    }

    private Collection flattenNavigators(Collection soFar, FastNavigator nav) {
        soFar.add(nav);

        if (nav.getChildNavigator() != null) {
            flattenNavigators(soFar, nav.getChildNavigator());
        }

        return soFar;
    }

    private void collectModifiers(IQueryResult result, FastSearchResult searchResult) {
        for (Iterator iterator = navigatedTo.keySet().iterator(); iterator.hasNext();) {
            String navigatorKey = (String) iterator.next();

            collectModifier(navigatorKey, result, searchResult);
        }
    }

    private void collectModifier(String navigatorKey, IQueryResult result, FastSearchResult searchResult) {
        FastNavigator nav = (FastNavigator) navigatedTo.get(navigatorKey);

        INavigator navigator = result.getNavigator(nav.getName());

        if (navigator != null) {

            Iterator modifers = navigator.modifiers();

            while (modifers.hasNext()) {
                IModifier modifier = (IModifier) modifers.next();
                Modifier mod = new Modifier(modifier.getName(), modifier.getCount(), nav);
                searchResult.addModifier(navigatorKey, mod);
            }

        } else if (nav.getChildNavigator() != null) {
            navigatedTo.put(navigatorKey, nav.getChildNavigator());
            collectModifier(navigatorKey, result, searchResult);
        }
    }


    public Map getOtherNavigators(String navigatorKey) {

        Map otherNavigators = new HashMap();

        for (Iterator iterator = getParameters().keySet().iterator(); iterator.hasNext();) {

            String parameterName = (String) iterator.next();

            if (parameterName.startsWith("nav_") && !parameterName.substring(parameterName.indexOf('_') + 1).equals(navigatorKey)) {
                String paramValue[] = (String[]) getParameters().get(parameterName);
                otherNavigators.put(parameterName.substring(parameterName.indexOf('_') + 1), paramValue[0]);
            }
        }
        return otherNavigators;
    }

    public void setSearchEngineFactory(IFastSearchEngineFactory factory) {
        engineFactory = factory;
    }

    public void addNavigatedTo(String navigatorKey, String navigatorName) {
        FastNavigator navigator = fastConfiguration.getNavigator(navigatorKey);

        if (navigatorName == null) {
            navigatedTo.put(navigatorKey, navigator);
        } else {
            navigatedTo.put(navigatorKey, findChildNavigator(navigator, navigatorName));
        }
    }

    public FastNavigator getNavigatedTo(String navigatorKey) {
        return (FastNavigator) navigatedTo.get(navigatorKey);
    }


    public FastNavigator getParentNavigator(String navigatorKey) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {
            String navName[] = (String[]) getParameters().get("nav_" + navigatorKey);
            return findParentNavigator((FastNavigator) fastConfiguration.getNavigators().get(navigatorKey), navName[0]);
        } else {
            return null;
        }
    }

    public FastNavigator getParentNavigator(String navigatorKey, String name) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {
            return findParentNavigator((FastNavigator) fastConfiguration.getNavigators().get(navigatorKey), name);
        } else {
            return null;
        }
    }

    public FastNavigator findParentNavigator(FastNavigator navigator, String navigatorName) {
        if (navigator.getChildNavigator() == null) {
            return null;
        } else if (navigator.getChildNavigator().getName().equals(navigatorName)) {

            if (true) {
                return navigator;
            } else {
                return findParentNavigator(navigator.getChildNavigator(), navigatorName);

            }
        } else {
            return findParentNavigator(navigator.getChildNavigator(), navigatorName);
        }
    }

    public Map getNavigatedValues() {
        return navigatedValues;
    }

    public String getNavigatedValue(String fieldName) {
        String[] singleValue = (String[]) navigatedValues.get(fieldName);

        if (singleValue != null) {
            return (singleValue[0]);
        } else {
            return null;
        }
    }

    public boolean isTopLevelNavigator(String navigatorKey) {
        return !getParameters().containsKey("nav_" + navigatorKey);
    }

    public Map getNavigatedTo() {
        return navigatedTo;
    }

    public String getNavigatorTitle(String navigatorKey) {
        FastNavigator nav = getNavigatedTo(navigatorKey);

        FastNavigator parent = findParentNavigator(fastConfiguration.getNavigator(navigatorKey), nav.getName());

        String value = getNavigatedValue(nav.getField());

        if (value == null && parent != null) {

            value = getNavigatedValue(parent.getField());

            if (value == null) {
                parent = findParentNavigator(fastConfiguration.getNavigator(navigatorKey), parent.getName());
                
                if (parent != null) {
                    value = getNavigatedValue(parent.getField());
                }
                return value;
            } else {
                return value;
            }
        }

        if (value == null) {
            return nav.getDisplayName();
        } else {
            return value;
        }

    }

    public String getNavigatorTitle(FastNavigator navigator) {
        String value = getNavigatedValue(navigator.getField());

        if (value == null) {
            return navigator.getDisplayName();
        } else {
            return value;
        }
    }

    public List getNavigatorBackLinks(String navigatorKey) {
        List backLinks = addNavigatorBackLinks(fastConfiguration.getNavigator(navigatorKey), new ArrayList(), navigatorKey);

        if (backLinks.size() > 0) {
            backLinks.remove(backLinks.size() - 1);
        }

        return backLinks;
    }

    public List addNavigatorBackLinks(FastNavigator navigator, List links, String navigatorKey) {


        String a[] = (String[]) getParameters().get(navigator.getField());

        if (a != null) {

            log.debug(navigator.getName());
            log.debug(a[0]);

            if (!(navigator.getName().equals("ywfylkesnavigator") && a[0].equals("Oslo"))) {
                if (!(navigator.getName().equals("ywkommunenavigator") && a[0].equals("Oslo"))) {
                    links.add(navigator);
                }
            }
        }

        if (navigator.getChildNavigator() != null) {
            String n[] = (String[]) getParameters().get("nav_" + navigatorKey);

            if (n != null && navigator.getName().equals(n[0])) {
                return links;
            }

            addNavigatorBackLinks(navigator.getChildNavigator(), links, navigatorKey);
        }

        return links;
    }

    private FastNavigator findChildNavigator(FastNavigator nav, String nameToFind) {
        if (getParameters().containsKey(nav.getField())) {
            String navigatedValue[] = (String[]) getParameters().get(nav.getField());
            navigatedValues.put(nav.getField(), navigatedValue);
        }

        if (nav.getName().equals(nameToFind)) {
            if (nav.getChildNavigator() != null) {
                return nav.getChildNavigator();
            } else {
                return nav;
            }
        }

        if (nav.getChildNavigator() == null) {
            throw new RuntimeException("Navigator " + nameToFind + " not found.");
        }

        return findChildNavigator(nav.getChildNavigator(), nameToFind);
    }
}
