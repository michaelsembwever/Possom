/* Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * AbstractFast4SearchCommand.java
 *
 * Created on 14 March 2006, 19:51
 *
 */

package no.sesat.search.mode.command;



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
import no.fast.ds.search.IQueryTransformations;
import no.fast.ds.search.ISearchParameters;
import no.fast.ds.search.Query;
import no.fast.ds.search.SearchEngineException;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchParameters;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.mode.config.FastCommandConfig;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.Modifier;
import no.sesat.search.result.Navigator;
import no.sesat.search.site.config.SiteConfiguration;
import no.sesat.search.result.BasicWeightedSuggestion;
import no.sesat.search.result.ModifierDateComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.result.WeightedSuggestion;

/**
 * Handles the basic implementation of the FAST 4 search.
 *
 * Includes support for
 *  - faceted search (modifiers or navigators),
 *  - relevant queries, proper name and spelling suggestions.
 *
 * @version $Id$
 */
 public abstract class AbstractFast4SearchCommand extends AbstractSearchCommand {

    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(AbstractFast4SearchCommand.class);
    private static final String ERR_FAST_FAILURE = " suffered from a FAST error ";
    private static final String ERR_EXECUTE_FAILURE = "execute() failed";
    private static final String DEBUG_FAST_SEARCH_ENGINE = "Creating Fast Engine to ";
    private static final String DEBUG_EXECUTE_QR_URL = "execute() QueryServerURL=";
    private static final String DEBUG_EXECUTE_COLLECTIONS = "execute() Collections=";
    private static final String DEBUG_EXECUTE_QUERY = "execute() Query=";
    private static final String DEBUG_EXECUTE_FILTER = "execute() Filter=";
    private static final String COLLAPSE_PARAMETER = "collapse";

    // Attributes ----------------------------------------------------
    private final Map<String, Navigator> navigators;
    // Deprecated code. I cannot see how anybody is using it. Much related below is also commented out.
//    private final Map<String, String[]> navigatedValues = new HashMap<String, String[]>();

    private final String queryServerUrl;

    // Static --------------------------------------------------------
    private static final Map<String, IFastSearchEngine> SEARCH_ENGINES
            = new ConcurrentHashMap<String, IFastSearchEngine>();

    private static transient IFastSearchEngineFactory engineFactory;

    static {
        try {
            engineFactory = FastSearchEngineFactory.newInstance();

        } catch (ConfigurationException e) {
            LOG.fatal(e.getMessage(), e);
            // Who exactly is expected to catch from a static constructor?
            throw new SearchCommandException(e);
        }
    }

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of AbstractFast4SearchCommand
     * @param cxt the context the search command must work within.
     */
    public AbstractFast4SearchCommand(final Context cxt) {

        super(cxt);

        final FastCommandConfig conf = (FastCommandConfig) cxt.getSearchConfiguration();
        final SiteConfiguration siteConf = cxt.getDataModel().getSite().getSiteConfiguration();
        queryServerUrl = siteConf.getProperty(conf.getQueryServerUrl());
        navigators = conf.getNavigators();
    }

    // Public --------------------------------------------------------

    /** Return all our configured navigator's field:value pairs in one string in filter syntax.
     * Commas in the navigator's parameter value are treated as separators between optional selected navigator items.
     *
     * @deprecated @todo extract to a query transformer, FastNavigationQueryTransformer
     *
     * @return field:value filter string.
     */
    public Collection<String> createNavigationFilterStrings() {

        Collection<String> filterStrings = new ArrayList<String>();

//        for (final Iterator iterator = navigatedValues.keySet().iterator(); iterator.hasNext();) {
//            final String field = (String) iterator.next();
//
//            final String modifiers[] = navigatedValues.get(field);
//
//            for (int i = 0; i < modifiers.length; i++) {
//                if (!field.equals("contentsource") || !modifiers[i].equals("Norske nyheter")) {
//                    if ("adv".equals(getSearchConfiguration().getFiltertype()))
//                        filterStrings.add(" AND " + field + ":\"" + modifiers[i] + "\"");
//                    else
//                        filterStrings.add("+" + field + ":\"" + modifiers[i] + "\"");
//                }
//            }
//        }

        for (final Navigator navigator : getNavigators().values()) {

            filterStrings = createNavigationFilterStrings(filterStrings, navigator);
        }

        return filterStrings;
    }

    /**
     * set the IFastSearchEngineFactory to use when getting, or creating, IFastSearchEngines.
     * @param factory
     */
    public static void setSearchEngineFactory(final IFastSearchEngineFactory factory) {
        engineFactory = factory;
    }

    // Z implementation ----------------------------------------------

    // SearchCommand overrides ----------------------------------------------

    /**
     * Assured associated search configuration will always be of this type. *
     */
    @Override
    public FastCommandConfig getSearchConfiguration() {
        return (FastCommandConfig) super.getSearchConfiguration();
    }

    public ResultList<ResultItem> execute() {

        try {
            final IFastSearchEngine engine = getSearchEngine();

            final IQuery fastQuery = createQuery();

            IQueryResult result = null;
            try {

                LOG.debug(DEBUG_EXECUTE_QR_URL + queryServerUrl);
                LOG.debug(DEBUG_EXECUTE_COLLECTIONS + getSearchConfiguration().getCollections());
                LOG.debug(DEBUG_EXECUTE_QUERY + fastQuery.getQueryString());
                LOG.debug(DEBUG_EXECUTE_FILTER + getSearchConfiguration().getCollectionFilterString());

                result = engine.search(fastQuery);

            } catch (SocketTimeoutException ste) {

                LOG.error(getSearchConfiguration().getName() +  " --> " + ste.getMessage());
                return new FastSearchResult<ResultItem>();

            } catch (IOException ioe) {

                LOG.error(getSearchConfiguration().getName() + ERR_FAST_FAILURE, ioe);
                return new FastSearchResult<ResultItem>();

            } catch (SearchEngineException fe) {

                LOG.error( getSearchConfiguration().getName() + ERR_FAST_FAILURE + '[' + fe.getErrorCode() + ']', fe);
                return new FastSearchResult<ResultItem>();

            }

            DUMP.info(fastQuery.toString());

            final FastSearchResult<ResultItem> searchResult = collectResults(result);

            if (getSearchConfiguration().isSpellcheck()) {
                collectSpellingSuggestions(result, searchResult);
            }


            if (getSearchConfiguration().isRelevantQueries() && null == getParameter("qs")) {
                collectRelevantQueries(result, searchResult);
            }


            if (getNavigators() != null) {
                collectModifiers(result, searchResult);
            }

            final String collapseId = getParameter(COLLAPSE_PARAMETER);

            if (getSearchConfiguration().isCollapsing() && getSearchConfiguration().isExpansion()) {

                if (collapseId != null && !collapseId.equals("")) {

                    if (searchResult.getResults().size() > 0) {
                        final ResultItem itm = searchResult.getResults().get(0);
                        final URL url = new URL(itm.getField("url"));
                        searchResult.addField("collapsedDomain", url.getHost());
                    }
                }
            }

            return searchResult;

        } catch (ConfigurationException e) {
            LOG.error(ERR_EXECUTE_FAILURE, e);
            throw new SearchCommandException(e);

        } catch (MalformedURLException e) {
            LOG.error(ERR_EXECUTE_FAILURE, e);
            throw new SearchCommandException(e);
        }
    }

    /**
     * @deprecated does nothing. here only to maintain API.
     */
    public void addNavigatedTo(final String navigatorKey) {}

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /**
     * Get the navigators from the command's SearchConfiguration.
     * @return the navigators
     */
    protected Map<String, Navigator> getNavigators() {

        return navigators;
    }


    @Override
    protected int getResultsToReturn() {

        return getSearchConfiguration().getResultsToReturn();
    }

    /**
     * Get, or create, the IFastSearchEngine responsible for connecting to and fetching the results from the fsat index.
     * @return the IFastSearchEngine
     * @throws ConfigurationException
     * @throws MalformedURLException index url is malformed
     */
    protected IFastSearchEngine getSearchEngine() throws ConfigurationException, MalformedURLException {
        try {
            if (!SEARCH_ENGINES.containsKey(queryServerUrl)) {
                LOG.debug(DEBUG_FAST_SEARCH_ENGINE + getSearchConfiguration().getQueryServerUrl() + "-->" + queryServerUrl);

                final IFastSearchEngine engine = engineFactory.createSearchEngine(queryServerUrl);
                SEARCH_ENGINES.put(queryServerUrl, engine);
            }
            return SEARCH_ENGINES.get(queryServerUrl);
        } catch (MalformedURLException e) {
            LOG.error("Malformed URL is: " + queryServerUrl);
            throw(e);
        }
    }

    protected String getSortBy() {

        return getSearchConfiguration().getSortBy();
    }

    /** Add additional fast search parameters.
     * iSearchParameters.setParameter(new SearchParameter(...));
     *
     * @param iSearchParameters live list of parameters being built and eventually used in the fast query.
     */
    protected void setAdditionalParameters(final ISearchParameters iSearchParameters) {

        for(Map.Entry<String,String> entry : getSearchConfiguration().getSearchParameterMap().entrySet()){
            iSearchParameters.setParameter(new SearchParameter(entry.getKey(), entry.getValue()));
        }
    }

    // Private -------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void collectSpellingSuggestions(final IQueryResult result, final FastSearchResult searchResult) {

        final IQueryTransformations qTransforms = result.getQueryTransformations(false);
        if (qTransforms.getSuggestions().size() > 0) {
            for (IQueryTransformation transformation
                    : (Collection<IQueryTransformation>) qTransforms.getAllQueryTransformations()) {

                if (transformation.getName().equals("FastQT_SpellCheck") && transformation.getAction().equals("nop")) {
                    final String custom = transformation.getCustom();
                    final WeightedSuggestion suggestion = createSpellingSuggestion(custom);
                    searchResult.addSpellingSuggestion(suggestion);
                }

                if (transformation.getName().equals("FastQT_ProperName")) {
                    String custom = transformation.getCustom();
                    WeightedSuggestion suggestion = createProperNameSuggestion(custom);
                    if (suggestion != null) {
                        searchResult.addSpellingSuggestion(suggestion);
                    }
                }
            }
        }

        if ("42".equals(datamodel.getQuery().getString())) {
            final WeightedSuggestion egg = BasicWeightedSuggestion.instanceOf("42", "Meningen med livet", "Meningen med livet", 1000);
            searchResult.addSpellingSuggestion(egg);
        }

        if ("kvasir".equalsIgnoreCase(datamodel.getQuery().getString())) {
            final WeightedSuggestion egg = BasicWeightedSuggestion.instanceOf("kvasir", "sesam", "sesam", 1000);
            searchResult.addSpellingSuggestion(egg);
        }

        if ("meningen med livet".equalsIgnoreCase(datamodel.getQuery().getString())) {
            final WeightedSuggestion egg = BasicWeightedSuggestion.instanceOf("meningen med livet", "42", "42", 1000);
            searchResult.addSpellingSuggestion(egg);
        }
    }

    private WeightedSuggestion createSpellingSuggestion(final String custom) {

        final int suggestionIndex = custom.indexOf("->");
        final int qualityIndex = custom.indexOf("Quality:");

        LOG.debug("Custom is " + custom);

        final String orig = custom.substring(0, suggestionIndex);
        final String string = custom.substring(suggestionIndex + 2, qualityIndex - 2);
        final String quality = custom.substring(qualityIndex + 9, qualityIndex + 12);

        return BasicWeightedSuggestion.instanceOf(orig, string, string, Integer.parseInt(quality));
    }

    private FastSearchResult<ResultItem> collectResults(final IQueryResult result) {

        if (LOG.isDebugEnabled()) {

            LOG.debug(getSearchConfiguration().getName() + " Collecting results. There are " + result.getDocCount());
            LOG.debug(getSearchConfiguration().getName() + " Number of results to collect: " + getSearchConfiguration().getResultsToReturn());

        }

        final FastSearchResult<ResultItem> searchResult = new FastSearchResult<ResultItem>();
        final int cnt = getOffset();

        final int maxIndex = Math.min(cnt + getResultsToReturn(), result.getDocCount());

        searchResult.setHitCount(result.getDocCount());

        for (int i = cnt; i < maxIndex; i++) {
            final IDocumentSummary document = result.getDocument(i + 1);
            //catch nullpointerException because of unaccurate doccount
            try {
                final ResultItem item = createResultItem(document);
                searchResult.addResult(item);
            } catch (NullPointerException e) {
                LOG.debug("Error finding document", e);
                return searchResult;
            }
        }
        return searchResult;
    }

    private ResultItem createResultItem(final IDocumentSummary document) {

        ResultItem item = new BasicResultItem();

        for (final Map.Entry<String, String> entry : getSearchConfiguration().getResultFieldMap().entrySet()) {
            final IDocumentSummaryField summary = document.getSummaryField(entry.getKey());

            if (summary != null) {
                item = item.addField(entry.getValue(), summary.getSummary());
            }
        }


        if (getSearchConfiguration().isCollapsing() && getSearchConfiguration().isExpansion()) {
            final String currCollapseId = getParameter(COLLAPSE_PARAMETER);

            if (currCollapseId == null || currCollapseId.equals("")) {
                final String moreHits = document.getSummaryField("morehits").getSummary();

                if (moreHits.equals("1")) {
                    item = item.addField("moreHits", "true")
                            .addField("collapseParameter", COLLAPSE_PARAMETER)
                            .addField("collapseId", document.getSummaryField("collapseid").getSummary());
                }
            }
        }

        return item;
    }

    private IQuery createQuery() {

        final ISearchParameters params = new SearchParameters();
        params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE, getSearchConfiguration().isLemmatise()));

        params.setParameter(new SearchParameter(
                "sesat:uniqueId",
                context.getDataModel().getParameters().getUniqueId()));

        if (getSearchConfiguration().isSpellcheck()) {
            params.setParameter(new SearchParameter(BaseParameter.SPELL, "suggest"));
            params.setParameter(new SearchParameter("qtf_spellcheck:addconsidered", "1"));
            params.setParameter(new SearchParameter("qtf_spellcheck:consideredverbose", "1"));
        }

        if (getSearchConfiguration().getName() != null && getSearchConfiguration().getName().equals("relevantQueries")) {
            params.setParameter(new SearchParameter("sources", "alone"));
        }

        String kwString = "";
        String queryString = getTransformedQuery();

        if (getSearchConfiguration().isKeywordClusteringEnabled()) {
            if (null != getParameter("kw")) {
                kwString = getParameter("kw");
            }

            if (!kwString.equals("")) {
                queryString += " " + kwString;
            }
        }
        // TODO: This is a little bit messy
        // Set filter, the filtertype may be adv
        final StringBuilder filter = new StringBuilder(getSearchConfiguration().getCollectionFilterString());


        if (!getSearchConfiguration().isIgnoreNavigation() && getNavigators() != null) {

            final Collection<String> navStrings = createNavigationFilterStrings();
            filter.append(' ');
            filter.append(' ').append(StringUtils.join(navStrings.iterator(), " "));
        }

        if (getSearchConfiguration().getOffensiveScoreLimit() > 0) {
            filter.append(' ').append("-ocfscore:>").append(getSearchConfiguration().getOffensiveScoreLimit());

        }

        if (getSearchConfiguration().getSpamScoreLimit() > 0) {
            filter.append(' ').append("+spamscore:<").append(getSearchConfiguration().getSpamScoreLimit());
        }


        final String collapseId = getParameter(COLLAPSE_PARAMETER);

        if (getSearchConfiguration().isCollapsing()) {
            if (null == collapseId || "".equals(collapseId) || !getSearchConfiguration().isExpansion()) {
                params.setParameter(new SearchParameter(BaseParameter.COLLAPSING, true));

            } else {
                params.setParameter(new SearchParameter(BaseParameter.COLLAPSING, false));
                filter.append(" +collapseid:").append(collapseId);
            }
        }

        if (getSearchConfiguration().getFilter() != null && getSearchConfiguration().getFilter().length() > 0) {
            // TODO create NowMacroFilterQueryTransfomer to do this instead
            final Calendar c = Calendar.getInstance();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            final String updatedFilter = getSearchConfiguration().getFilter()
                    .replaceAll("\\{NOW\\}", sdf.format(c.getTime()));

            filter.append(' ' + updatedFilter);
        }

        // Init dynamic filters
        final String superFilter = null == getFilter() ? "" : getFilter();

        LOG.debug("createQuery: superFilter=" + superFilter);

        params.setParameter(new SearchParameter("filtertype",
                "adv".equals(getSearchConfiguration().getFiltertype())?  "adv": "any" ));

        params.setParameter(new SearchParameter(BaseParameter.TYPE, getSearchConfiguration().getQueryType()));

        params.setParameter(new SearchParameter(BaseParameter.FILTER, filter.toString() + ' ' + superFilter));

        if (getSearchConfiguration().getQtPipeline() != null && getSearchConfiguration().getQtPipeline().length() > 0) {
            params.setParameter(
                    new SearchParameter(BaseParameter.QTPIPELINE, getSearchConfiguration().getQtPipeline()));
        }
        params.setParameter(new SearchParameter(BaseParameter.QUERY, queryString));
        params.setParameter(new SearchParameter(BaseParameter.COLLAPSING, getSearchConfiguration().isCollapsing()));
        params.setParameter(
                new SearchParameter(BaseParameter.LANGUAGE, getSearchConfiguration().getSpellchecklanguage()));

        if (getNavigators() != null && getNavigators().size() > 0) {
            params.setParameter(new SearchParameter(BaseParameter.NAVIGATION, true));
        }

        params.setParameter(new SearchParameter("hits", getResultsToReturn()));
        params.setParameter(new SearchParameter(BaseParameter.CLUSTERING, getSearchConfiguration().isClustering()));

        if (getSearchConfiguration().getResultView() != null && getSearchConfiguration().getResultView().length() > 0) {
            params.setParameter(
                    new SearchParameter(BaseParameter.RESULT_VIEW, getSearchConfiguration().getResultView()));

        }

        // default SORT_BY is defined by configuration
        if (getSortBy() != null && getSortBy().length() > 0) {
            params.setParameter(new SearchParameter(BaseParameter.SORT_BY, getSortBy()));
        }

        // XXX Move-out. This code is specific to Norway's mobileYellowGeo mode!
        if (null != getParameter("c") && getParameter("c").equals("yg")) {
            if (getParameter("type").equals("f")) {
                params.setParameter(new SearchParameter(
                        "qtf_geosearch:center",
                        '(' + getParameter("cla") + ',' + getParameter("clo")));

                params.setParameter(new SearchParameter(
                        "qtf_geosearch:filterbox",
                        "[(" + getParameter("la1") + "," + getParameter("lo1") + ");("
                                + getParameter("la2") + ',' + getParameter("lo2") + ")]"));

                params.setParameter(new SearchParameter("sortdirection", "ascending"));

            } else {
                params.setParameter(new SearchParameter(
                        "qtf_geosearch:center",
                        '(' + getParameter("cla") + "," + getParameter("clo")));

                params.setParameter(new SearchParameter("qtf_geosearch:radius", getParameter("rad")));
                params.setParameter(new SearchParameter("sortdirection", "ascending"));
            }
        } // <-- END-OF move-out

        if (null != getParameter("rank")) {
            params.setParameter(new SearchParameter(BaseParameter.SORT_BY, getParameter("rank")));
        }

        // This  now uses sort order from canfiguration, old ones still here for backwards compability untill all are uppdated
        if (isUserSortable()) {

            final String sortBy = getUserSortBy();
            LOG.debug("userSortBy " + sortBy);

            // TODO move-out to genericno. this is configuration hardcoded.
            if("default".equals(sortBy)) {
                 params.setParameter(new SearchParameter(BaseParameter.SORT_BY, getSearchConfiguration().getSortBy()));

            }else if ("alternative".equals(sortBy) && getSearchConfiguration().getAlternativeSortBy() != null ) {
                 params.setParameter(new SearchParameter(BaseParameter.SORT_BY, getSearchConfiguration().getAlternativeSortBy()));

            }else if ("standard".equals(sortBy)) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "retriever"));

            } else if ("datetime".equals(sortBy)) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "docdatetime+standard"));

            } else if ("-datetime".equals(sortBy)) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "+docdatetime"));

            } // <-- END-OF move-out.
        }

        params.setParameter(new SearchParameter(BaseParameter.NAVIGATORS, getNavigatorsString()));

        setAdditionalParameters(params);

        return new Query(params);
    }

    private String getNavigatorsString() {

        final Collection<Navigator> allFlattened = new ArrayList<Navigator>();

        for (Navigator navigator : getNavigators().values()) {

            allFlattened.addAll(flattenNavigators(new ArrayList<Navigator>(), navigator));
        }

        return StringUtils.join(allFlattened.iterator(), ',');
    }

    private Collection<Navigator> flattenNavigators(Collection<Navigator> soFar, Navigator nav) {

        soFar.add(nav);

        // don't ask for navigators one step beyond anything not selected
        if (null != nav.getChildNavigator() && null != datamodel.getParameters().getValue(nav.getId())) {
            flattenNavigators(soFar, nav.getChildNavigator());
        }

        return soFar;
    }

    private void collectModifiers(IQueryResult result, FastSearchResult<ResultItem> searchResult) {

        for (Navigator nav : getNavigators().values()) {

            collectModifier(nav, result, searchResult);
        }
    }

    private void collectModifier(
            final Navigator nav,
            final IQueryResult result,
            final FastSearchResult<ResultItem> searchResult) {

        final String navigatorKey = nav.getId();

        final INavigator navigator = result.getNavigator(nav.getName());

        if (null != navigator) {

            final Iterator iterator = navigator.modifiers();

            while (iterator.hasNext()) {

                final IModifier modifier = (IModifier) iterator.next();

                if (!"unknown".equals(modifier.getName())) {

                    searchResult.addModifier(
                            navigatorKey,
                            new Modifier(modifier.getName(), modifier.getCount(), nav));
                }
            }

            // live copy of list!
            final List<Modifier> modifiers = searchResult.getModifiers(navigatorKey);

            if (null != modifiers) {
                switch (nav.getSort()) {
                    case DAY_MONTH_YEAR:
                        Collections.sort(modifiers, ModifierDateComparator.DAY_MONTH_YEAR);
                        break;
                    case DAY_MONTH_YEAR_DESCENDING:
                        Collections.sort(modifiers, ModifierDateComparator.DAY_MONTH_YEAR_DESCENDING);
                        break;
                    case YEAR_MONTH_DAY_DESCENDING:
                        Collections.sort(modifiers, ModifierDateComparator.YEAR_MONTH_DAY_DESCENDING);
                        break;
                    case YEAR:
                        Collections.sort(modifiers, ModifierDateComparator.YEAR);
                        break;
                    case MONTH_YEAR:
                        Collections.sort(modifiers, ModifierDateComparator.MONTH_YEAR);
                        break;
                    case CUSTOM:
                        Collections.sort(modifiers, getModifierComparator(nav));
                        break;
                    case NONE:
                        // Use the sorting the index returns
                        break;
                    case COUNT:
                        /* Fall through */
                    default:
                        Collections.sort(modifiers);
                        break;
                }
            }

            // include any child
            if(null != nav.getChildNavigator()){
                collectModifier(nav.getChildNavigator(), result, searchResult);
            }
        }
    }

    /** Override to provide custom modifier comparators.
     * This implementation always return null.
     *
     * @param nav
     * @return null
     */
    protected Comparator<Modifier> getModifierComparator(final Navigator nav) {
        return null;
    }

    private WeightedSuggestion createProperNameSuggestion(String custom) {

        int suggestionIndex = custom.indexOf("->");

        LOG.debug("Custom is " + custom);

        String orig = custom.substring(0, suggestionIndex);
        String string = custom.substring(suggestionIndex + 2);

        string = string.replaceAll("\"", "");

        if (orig.equalsIgnoreCase(string)) {
            return null;
        }

        return BasicWeightedSuggestion.instanceOf(orig, string, string, 1000);
    }

    @SuppressWarnings("unchecked")
    private void collectRelevantQueries(IQueryResult result, FastSearchResult searchResult) {

        if (result.getQueryTransformations(false).getSuggestions().size() > 0) {

            for (IQueryTransformation transformation
                    : (Collection<IQueryTransformation>)result
                            .getQueryTransformations(false).getAllQueryTransformations()){

                if (transformation.getName().equals("FastQT_Synonym") && transformation.getMessageID() == 8) {

                    final String query = transformation.getQuery();
                    final String[] forWords = query.split("#!#");

                    for (int i = 0; i < forWords.length; i++) {

                        final String[] forOneWord = forWords[i].split("###");

                        for (int j = 0; j < forOneWord.length; j++) {

                            final String[] suggNweight = forOneWord[j].split("@");

                            if (!datamodel.getQuery().getString().equalsIgnoreCase(suggNweight[0])) {

                                final WeightedSuggestion rq = BasicWeightedSuggestion.instanceOf(
                                        getQuery().getQueryString(),
                                        suggNweight[0],
                                        suggNweight[0],
                                        2 == suggNweight.length ? Integer.valueOf(suggNweight[1]) : Integer.MIN_VALUE);

                                searchResult.addRelevantQuery(rq);
                            }
                        }
                    }
                }
            }
        }
    }


    private Collection<String> createNavigationFilterStrings(
             final Collection<String> filterStrings,
             final Navigator navigator) {

         final StringDataObject navigatedValue = datamodel.getParameters().getValue(navigator.getId());

         if(null != navigatedValue){

             // TODO this test should be encapsulated with the delegated filterBuilder
             final StringBuilder filter = new StringBuilder("adv".equals(getSearchConfiguration().getFiltertype())
                     ? " AND ("
                     : " +(");

             // splitting here allows for multiple navigation selections within the one navigation level.
             for (String navSingleValue : navigatedValue.getString().split(",")) {

                 final String value = navigator.isBoundaryMatch()
                         ? "^\"" + navSingleValue + "\"$"
                         : "\"" + navSingleValue + "\"";

                 filter.append(' ' + navigator.getField() + ':' + value);
             }

             filterStrings.add(filter.append(" )").toString());
        }

         // include any children (if parent is selected)
         return null != navigatedValue && null != navigator.getChildNavigator()
                 ? createNavigationFilterStrings(filterStrings, navigator.getChildNavigator())
                 : filterStrings;
     }

    // Inner classes -------------------------------------------------

}
