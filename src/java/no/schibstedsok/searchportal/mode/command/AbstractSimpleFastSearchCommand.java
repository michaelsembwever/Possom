/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractSimpleFastSearchCommand.java
 *
 * Created on 14 March 2006, 19:51
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import no.fast.ds.search.NoSuchParameterException;
import no.fast.ds.search.Query;
import no.fast.ds.search.SearchEngineException;
import no.fast.ds.search.SearchParameter;
import no.fast.ds.search.SearchParameters;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.mode.config.FastSearchConfiguration;
import no.schibstedsok.searchportal.mode.config.FastNavigator;
import no.schibstedsok.searchportal.mode.command.*;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.XorClause;

import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import no.schibstedsok.searchportal.view.spell.RelevantQuery;

import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/** Handles the basic implementation of the Simple FAST search.
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractSimpleFastSearchCommand extends AbstractSearchCommand {

    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(AbstractSimpleFastSearchCommand.class);
    private static final String ERR_FAST_FAILURE = " suffered from a FAST error ";
    private static final String ERR_EXECUTE_FAILURE = "execute() failed";
    private static final String INFO_CONSTRUCTED_QUERY = "Constructed ";
    private static final String DEBUG_FAST_SEARCH_ENGINE ="Creating Fast Engine to ";
    private static final String DEBUG_QUERY_DUMP = "QUERY DUMP: ";
    private static final String DEBUG_EXECUTE_QR_URL = "execute() QueryServerURL=";
    private static final String DEBUG_EXECUTE_COLLECTIONS = "execute() Collections=";
    private static final String DEBUG_EXECUTE_QUERY = "execute() Query=";
    private static final String DEBUG_EXECUTE_FILTER = "execute() Filter=";
    private static final String DEBUG_PARAM_NOT_FOUND = "Param not found ";

    // Attributes ----------------------------------------------------
    private final Map<String,FastNavigator> navigatedTo = new HashMap<String,FastNavigator>();
    private final Map<String,String[]> navigatedValues = new HashMap<String,String[]>();

    // Static --------------------------------------------------------
    private static final Map<String,IFastSearchEngine> SEARCH_ENGINES = new HashMap<String,IFastSearchEngine>();
    private static transient IFastSearchEngineFactory engineFactory;

    static {
        try {
            engineFactory = FastSearchEngineFactory.newInstance();
        } catch (ConfigurationException e) {
            throw new InfrastructureException(e);
        }
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of AbstractSimpleFastSearchCommand */
    public AbstractSimpleFastSearchCommand(
                    final Context cxt,
                    final Map parameters) {

        super(cxt, parameters);
    }

    // Public --------------------------------------------------------

    /** TODO comment me. **/
    public Collection createNavigationFilterStrings() {
        final Collection filterStrings = new ArrayList();

        for (final Iterator iterator = navigatedValues.keySet().iterator(); iterator.hasNext();) {
            final String field = (String) iterator.next();

            final String modifiers[] = (String[]) navigatedValues.get(field);


            for (int i = 0; i < modifiers.length; i++) {
                if (!field.equals("contentsource") || !modifiers[i].equals("Norske nyheter")){
                    filterStrings.add("+" + field + ":\"" + modifiers[i] + "\"");
                }
            }
        }

        return filterStrings;
    }

    /** TODO comment me. **/
    public Map getOtherNavigators(final String navigatorKey) {

        final Map<String,String> otherNavigators = new HashMap<String,String>();

        for (String parameterName : (Set<String>)getParameters().keySet()) {

            if (parameterName.startsWith("nav_") && !parameterName.substring(parameterName.indexOf('_') + 1).equals(navigatorKey)) {
                final String paramValue = getParameter(parameterName);
                otherNavigators.put(parameterName.substring(parameterName.indexOf('_') + 1), paramValue);
            }
        }
        return otherNavigators;
    }

    /** TODO comment me. **/
    public static void setSearchEngineFactory(final IFastSearchEngineFactory factory) {
        engineFactory = factory;
    }

    /** TODO comment me. **/
    public void addNavigatedTo(final String navigatorKey, final String navigatorName) {

        final FastNavigator navigator = (FastNavigator) getNavigators().get(navigatorKey);

        if (navigatorName == null) {
            navigatedTo.put(navigatorKey, navigator);
        } else {
            navigatedTo.put(navigatorKey, findChildNavigator(navigator, navigatorName));
        }
    }

    /** TODO comment me. **/
    public FastNavigator getNavigatedTo(final String navigatorKey) {
        return (FastNavigator) navigatedTo.get(navigatorKey);
    }


    /** TODO comment me. **/
    public FastNavigator getParentNavigator(final String navigatorKey) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {
            final String navName =  getParameter("nav_" + navigatorKey);

            return findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), navName);

        } else {
            return null;
        }
    }

    /** TODO comment me. **/
    public FastNavigator getParentNavigator(final String navigatorKey, final String name) {
        if (getParameters().containsKey("nav_" + navigatorKey)) {

            return findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), name);

        } else {
            return null;
        }
    }

    /** TODO comment me. **/
    public FastNavigator findParentNavigator(final FastNavigator navigator, final String navigatorName) {
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

    /** TODO comment me. **/
    public Map getNavigatedValues() {
        return navigatedValues;
    }

    /** TODO comment me. **/
    public String getNavigatedValue(final String fieldName) {
        final String[] singleValue = (String[]) navigatedValues.get(fieldName);

        if (singleValue != null) {
            return (singleValue[0]);
        } else {
            return null;
        }
    }

    /** TODO comment me. **/
    public boolean isTopLevelNavigator(final String navigatorKey) {
        return !getParameters().containsKey("nav_" + navigatorKey);
    }

    /** TODO comment me. **/
    public Map getNavigatedTo() {
        return navigatedTo;
    }

    /** TODO comment me. **/
    public String getNavigatorTitle(final String navigatorKey) {

        LOG.trace("getNavigatorTitle("+navigatorKey+")");
        final FastNavigator nav = getNavigatedTo(navigatorKey);

        FastNavigator parent = findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), nav.getName());

        String value = getNavigatedValue(nav.getField());

        if (value == null && parent != null) {

            value = getNavigatedValue(parent.getField());

            if (value == null) {

                parent = findParentNavigator((FastNavigator) getNavigators().get(navigatorKey), parent.getName());


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

    /** TODO comment me. **/
    public String getNavigatorTitle(final FastNavigator navigator) {

        final String value = getNavigatedValue(navigator.getField());

        if (value == null) {
            return navigator.getDisplayName();
        } else {
            return value;
        }
    }

    /** TODO comment me. **/
    public List getNavigatorBackLinks(final String navigatorKey) {

        final List backLinks = addNavigatorBackLinks(getSearchConfiguration().getNavigator(navigatorKey), new ArrayList(), navigatorKey);

        if (backLinks.size() > 0) {
            backLinks.remove(backLinks.size() - 1);
        }

        return backLinks;
    }

    /** TODO comment me. **/
    public List addNavigatorBackLinks(final FastNavigator navigator, final List links, final String navigatorKey) {

        final String a = getParameter(navigator.getField());

        if (a != null) {

            LOG.debug(navigator.getName());
            LOG.debug(a);

            if (!(navigator.getName().equals("ywfylkesnavigator") && a.equals("Oslo"))) {
                if (!(navigator.getName().equals("ywkommunenavigator") && a.equals("Oslo"))) {
                    links.add(navigator);
                }
            }
        }

        if (navigator.getChildNavigator() != null) {
            final String n = getParameter("nav_" + navigatorKey);

            if (n != null && navigator.getName().equals(n)) {
                return links;
            }

            addNavigatorBackLinks(navigator.getChildNavigator(), links, navigatorKey);
        }

        return links;
    }


    // Z implementation ----------------------------------------------

    // SearchCommand overrides ----------------------------------------------

    /** Assured associated search configuration will always be of this type. **/
    public FastSearchConfiguration getSearchConfiguration() {
        return (FastSearchConfiguration) super.getSearchConfiguration();
    }

    /** TODO comment me. **/
    public SearchResult execute() {

        try {
            if (getNavigators() != null) {
                for (String navigatorKey : getNavigators().keySet()) {

                    addNavigatedTo(navigatorKey, getParameters().containsKey("nav_" + navigatorKey)
                            ? getParameter("nav_" + navigatorKey)
                            : null);
                }
            }

            final IFastSearchEngine engine = getSearchEngine();
            final IQuery fastQuery = createQuery();

            IQueryResult result = null;
            try {
                
                LOG.debug(DEBUG_EXECUTE_QR_URL + getSearchConfiguration().getQueryServerURL());
                LOG.debug(DEBUG_EXECUTE_COLLECTIONS + getSearchConfiguration().getCollections());
                LOG.debug(DEBUG_EXECUTE_QUERY + fastQuery.getQueryString());
                LOG.debug(DEBUG_EXECUTE_FILTER + getSearchConfiguration().getCollectionFilterString());

                result = engine.search(fastQuery);

            } catch (IOException ioe) {
                LOG.error(getSearchConfiguration().getName() + ERR_FAST_FAILURE, ioe);
                return new FastSearchResult(this);
            } catch (SearchEngineException fastException) {
                LOG.error(
                        getSearchConfiguration().getName() + ERR_FAST_FAILURE + '[' + fastException.getErrorCode() + ']', 
                        fastException);
                return new FastSearchResult(this);
            }


            LOG.info(DEBUG_QUERY_DUMP + fastQuery);
            
            final FastSearchResult searchResult = collectResults(result);

            if (getSearchConfiguration().isSpellcheck()) {
                collectSpellingSuggestions(result, searchResult);
            }


            if (getSearchConfiguration().isRelevantQueries() && !getParameters().containsKey("qs")) {
                collectRelevantQueries(result, searchResult);
            }


            if (getNavigators() != null) {
                collectModifiers(result, searchResult);
            }

            return searchResult;

        } catch (ConfigurationException e) {
            LOG.error(ERR_EXECUTE_FAILURE, e);
            throw new InfrastructureException(e);

        } catch (MalformedURLException e) {
            LOG.error(ERR_EXECUTE_FAILURE, e);
            throw new InfrastructureException(e);
        }
    }


    // AbstractReflectionVisitor overrides ----------------------------------------------

    private boolean insideNot = false;
    private Boolean writeAnd = null;

    /** TODO comment me. **/
    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            final String transformedTerm = (String) getTransformedTerm(clause);
            if (transformedTerm != null && transformedTerm.length() > 0) {
                if (insideNot) {
                    appendToQueryRepresentation("-");
                }  else if (writeAnd != null && writeAnd.booleanValue()) {
                    appendToQueryRepresentation("+");
                }
                appendToQueryRepresentation(transformedTerm);
            }
        }
    }
    /** TODO comment me. **/
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final AndClause clause) {
        final Boolean originalWriteAnd = writeAnd;
        writeAnd = Boolean.TRUE;
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
        writeAnd = originalWriteAnd;
    }
    /** TODO comment me. **/
    protected void visitImpl(final OrClause clause) {
        final Boolean originalWriteAnd = writeAnd;
        writeAnd = Boolean.FALSE;
        appendToQueryRepresentation(" (");
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(") ");
        writeAnd = originalWriteAnd;
    }
    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final NotClause clause) {
        if (writeAnd == null) {
            // must start prefixing terms with +
            writeAnd = Boolean.TRUE;
        }
        final boolean originalInsideAndNot = insideNot;
        insideNot = true;
        clause.getFirstClause().accept(this);
        insideNot = originalInsideAndNot;

    }
    /** TODO comment me. **/
    protected void visitImpl(final AndNotClause clause) {
        if (writeAnd == null) {
            // must start prefixing terms with +
            writeAnd = Boolean.TRUE;
        }
        final boolean originalInsideAndNot = insideNot;
        insideNot = true;
        clause.getFirstClause().accept(this);
        insideNot = originalInsideAndNot;
    }
    /** TODO comment me. **/
    protected void visitImpl(final XorClause clause) {
        // [TODO] we need to determine which branch in the query-tree we want to use.
        //  Both branches to a XorClause should never be used.
        clause.getFirstClause().accept(this);
        // clause.getSecondClause().accept(this);
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** TODO comment me. **/
    protected Map<String,FastNavigator> getNavigators() {

        return getSearchConfiguration().getNavigators();
    }

    /** TODO comment me. **/
    protected int getResultsToReturn() {

        return getSearchConfiguration().getResultsToReturn();
    }

    /** TODO comment me. **/
    protected IFastSearchEngine getSearchEngine() throws ConfigurationException, MalformedURLException {

        if (!SEARCH_ENGINES.containsKey(getSearchConfiguration().getQueryServerURL())) {
            LOG.debug(DEBUG_FAST_SEARCH_ENGINE + getSearchConfiguration().getQueryServerURL());
            final IFastSearchEngine engine
                    = engineFactory.createSearchEngine(getSearchConfiguration().getQueryServerURL());
            SEARCH_ENGINES.put(getSearchConfiguration().getQueryServerURL(), engine);
        }
        return (IFastSearchEngine) SEARCH_ENGINES.get(getSearchConfiguration().getQueryServerURL());
    }

    protected String getSortBy() {

        return getSearchConfiguration().getSortBy();
    }


    // Private -------------------------------------------------------

    private void collectSpellingSuggestions(final IQueryResult result, final FastSearchResult searchResult) {

        final IQueryTransformations qTransforms = result.getQueryTransformations(false);
        if (qTransforms.getSuggestions().size() > 0) {
            for (IQueryTransformation transformation
                    : (Collection<IQueryTransformation>)qTransforms.getAllQueryTransformations()) {

                if (transformation.getName().equals("FastQT_SpellCheck") && transformation.getAction().equals("nop")) {
                    final String custom = transformation.getCustom();
                    final SpellingSuggestion suggestion = createSpellingSuggestion(custom);
                    searchResult.addSpellingSuggestion(suggestion);
                }

                if (transformation.getName().equals("FastQT_ProperName")) {
                    String custom = transformation.getCustom();
                    SpellingSuggestion suggestion = createProperNameSuggestion(custom);
                    if (suggestion != null) {
                        searchResult.addSpellingSuggestion(suggestion);
                    }
                }
             }
        }

        if (context.getRunningQuery().getQueryString().equals("42")) {
            final SpellingSuggestion egg = new SpellingSuggestion("42", "Meningen med livet", 1000);
            searchResult.addSpellingSuggestion(egg);
        }

        if (context.getRunningQuery().getQueryString().equalsIgnoreCase("meningen med livet")) {
            final SpellingSuggestion egg = new SpellingSuggestion("meningen med livet", "42", 1000);
            searchResult.addSpellingSuggestion(egg);
        }
    }

    private SpellingSuggestion createSpellingSuggestion(final String custom) {

        final int suggestionIndex = custom.indexOf("->");
        final int qualityIndex = custom.indexOf("Quality:");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Custom is " + custom);
        }

        final String orig = custom.substring(0, suggestionIndex);
        final String string = custom.substring(suggestionIndex + 2, qualityIndex - 2);
        final String quality = custom.substring(qualityIndex + 9, qualityIndex + 12);

        return new SpellingSuggestion(orig, string, Integer.parseInt(quality));
    }

    private FastSearchResult collectResults(final IQueryResult result) {

        if (LOG.isDebugEnabled()) {

            LOG.debug(getSearchConfiguration().getName() + " Collecting results. There are " + result.getDocCount());
            LOG.debug(getSearchConfiguration().getName() + " Number of results to collect: " + getSearchConfiguration().getResultsToReturn());

        }

        final FastSearchResult searchResult = new FastSearchResult(this);
        final int cnt = getCurrentOffset(0);

        final int maxIndex = Math.min(cnt + getResultsToReturn(), result.getDocCount());

        searchResult.setHitCount(result.getDocCount());

        for (int i = cnt; i < maxIndex; i++) {
            final IDocumentSummary document = result.getDocument(i + 1);
            //catch nullpointerException because of unaccurate doccount
            try {
                final SearchResultItem item = createResultItem(document);
                searchResult.addResult(item);
            } catch (NullPointerException e) {
                if (LOG.isDebugEnabled()) LOG.debug("Error finding document " + e);
                return searchResult;
            }
        }
        return searchResult;
    }

    private SearchResultItem createResultItem(final IDocumentSummary document) {

        final SearchResultItem item = new BasicSearchResultItem();

        for (final Map.Entry<String,String> entry : getSearchConfiguration().getResultFields().entrySet()) {
            final IDocumentSummaryField summary = document.getSummaryField(entry.getKey());

            if (summary != null) {
                item.addField(entry.getValue(), summary.getSummary());
            }
        }
        return item;
    }

    private IQuery createQuery() {

        ISearchParameters params = new SearchParameters();
        params.setParameter(new SearchParameter(BaseParameter.LEMMATIZE, getSearchConfiguration().isLemmatizeEnabled()));

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
            if (getParameters().containsKey("kw")) {
                kwString =  getParameters().get("kw") instanceof String[]
                        ? StringUtils.join((String[]) getParameters().get("kw"), " ")
                        : getParameter("kw");
            }

            if (!kwString.equals("")) {
                queryString += " " + kwString;
            }
        }
        // TODO: This is a little bit messy
        // Set filter, the filtertype may be adv
        final StringBuilder filter = new StringBuilder(getSearchConfiguration().getCollectionFilterString());


        if (!getSearchConfiguration().isIgnoreNavigation() && getNavigators() != null) {

            Collection navStrings = createNavigationFilterStrings();
            filter.append(" ");
            filter.append(" ").append(StringUtils.join(navStrings.iterator(), " "));
        }

        if (getSearchConfiguration().getOffensiveScoreLimit() > 0) {
            filter.append(" ").append("-ocfscore:>").append(getSearchConfiguration().getOffensiveScoreLimit());

        }

        if (getSearchConfiguration().getSpamScoreLimit() > 0) {
            filter.append(" ").append("+spamscore:<").append(getSearchConfiguration().getSpamScoreLimit());
        }

        if (getAdditionalFilter() != null) {
            filter.append(" ");
            filter.append(getAdditionalFilter());
        }

        if (getSearchConfiguration().getFilter() != null && getSearchConfiguration().getFilter().length() >0) {

            final Calendar c = Calendar.getInstance();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            final String updatedFilter = getSearchConfiguration().getFilter()
                    .replaceAll("\\{NOW\\}", sdf.format(c.getTime()));
            
            filter.append(' ' + updatedFilter);
        }

        // Init dynamic filters
        // TODO: Is the following used anywhere?
        String dynamicLanguage = getDynamicParams(getParameters(), "language", "");
        String dynamicFilterType = getDynamicParams(getParameters(), "filtertype", "any");
        String dynamicType = getDynamicParams(getParameters(), "type", "all");
        String superFilter = super.getFilter();

        if (superFilter == null) {
            superFilter = "";
        }


        if (LOG.isDebugEnabled()) {
            LOG.debug("createQuery: superFilter=" + superFilter);
        }
        params.setParameter(new SearchParameter("filtertype", dynamicFilterType));

        params.setParameter(new SearchParameter(BaseParameter.TYPE, dynamicType));

        params.setParameter(new SearchParameter(BaseParameter.FILTER,
                filter.toString() + " " + dynamicLanguage + " " + superFilter));

        if (getSearchConfiguration().getQtPipeline() != null && getSearchConfiguration().getQtPipeline().length() >0) {
            params.setParameter(new SearchParameter(BaseParameter.QTPIPELINE,
                    getSearchConfiguration().getQtPipeline()));
        }
        params.setParameter(new SearchParameter(BaseParameter.QUERY, queryString));
        params.setParameter(new SearchParameter(BaseParameter.COLLAPSING, getSearchConfiguration().isCollapsing()));

        params.setParameter(new SearchParameter(BaseParameter.LANGUAGE, "no"));


        if (getNavigators() != null && getNavigators().size() > 0) {
            params.setParameter(new SearchParameter(BaseParameter.NAVIGATION, true));
        }

        params.setParameter(new SearchParameter("hits", getResultsToReturn()));
        params.setParameter(new SearchParameter(BaseParameter.CLUSTERING, getSearchConfiguration().isClustering()));

        if (getSearchConfiguration().getResultView() != null && getSearchConfiguration().getResultView().length() >0) {
            params.setParameter(new SearchParameter(BaseParameter.RESULT_VIEW, getSearchConfiguration().getResultView()));
        }

        if (getSortBy() != null && getSortBy().length() >0) {
            params.setParameter(new SearchParameter(BaseParameter.SORT_BY, getSortBy()));
        }

        // TODO: Refactor
        if (getParameters().containsKey("userSortBy")) {

            String sortBy = getParameter("userSortBy");
            if (LOG.isDebugEnabled()) {
                LOG.debug("createQuery: SortBY " + sortBy);
            }
            if ("standard".equals(sortBy)) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "retriever"));
            } else if ("datetime".equals(sortBy)) {
                params.setParameter(new SearchParameter(BaseParameter.SORT_BY, "docdatetime+standard"));
            }
        }

        params.setParameter(new SearchParameter(BaseParameter.NAVIGATORS, getNavigatorsString()));

        IQuery query = new Query(params);

        LOG.info(INFO_CONSTRUCTED_QUERY + query);

        return query;
    }

    private String getDynamicParams(final Map map, final String key, final String defaultValue) {

        LOG.trace("getDynamicParams(map," + key +  "," + defaultValue + ")");

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
        LOG.trace("getDynamicParams returning " + value);
        return value;
    }

    private String getNavigatorsString() {

        if (getNavigators() != null) {


            Collection allFlattened = new ArrayList();


            for (FastNavigator navigator : getNavigators().values()) {

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

        for (String navigatorKey : navigatedTo.keySet()) {

            collectModifier(navigatorKey, result, searchResult);
        }

    }

    private void collectModifier(String navigatorKey, IQueryResult result, FastSearchResult searchResult) {

        final FastNavigator nav = (FastNavigator) navigatedTo.get(navigatorKey);

        INavigator navigator = result.getNavigator(nav.getName());

        if (navigator != null) {

            Iterator modifers = navigator.modifiers();

            while (modifers.hasNext()) {
                IModifier modifier = (IModifier) modifers.next();
                Modifier mod = new Modifier(modifier.getName(), modifier.getCount(), nav);
                searchResult.addModifier(navigatorKey, mod);
            }

            if (searchResult.getModifiers(navigatorKey) != null) {
                Collections.sort(searchResult.getModifiers(navigatorKey));
            }

        } else if (nav.getChildNavigator() != null) {
            navigatedTo.put(navigatorKey, nav.getChildNavigator());
            collectModifier(navigatorKey, result, searchResult);
        }
    }

    private FastNavigator findChildNavigator(FastNavigator nav, String nameToFind) {

        if (getParameters().containsKey(nav.getField())) {

            navigatedValues.put(nav.getField(), getParameters().get(nav.getField()) instanceof String[]
                    ? (String[])getParameters().get(nav.getField())
                    : new String[]{getParameter(nav.getField())});
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

    private SpellingSuggestion createProperNameSuggestion(String custom) {

        int suggestionIndex = custom.indexOf("->");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Custom is " + custom);
        }

        String orig = custom.substring(0, suggestionIndex);
        String string = custom.substring(suggestionIndex + 2);

        string = string.replaceAll("\"", "");

        if (orig.equalsIgnoreCase(string)) {
            return null;
        }

        return new SpellingSuggestion(orig, string, 1000);
    }

    private void collectRelevantQueries(IQueryResult result, FastSearchResult searchResult) {

        if (result.getQueryTransformations(false).getSuggestions().size() > 0) {
            for (Iterator iterator = result.getQueryTransformations(false).getAllQueryTransformations().iterator(); iterator.hasNext();) {
                IQueryTransformation transformation = (IQueryTransformation) iterator.next();

                if (transformation.getName().equals("FastQT_Synonym") && transformation.getMessageID() == 8) {
                    String query = transformation.getQuery();

                    String[] forWords = query.split("#!#");

                    for (int i = 0; i < forWords.length; i++) {
                        String[] forOneWord = forWords[i].split("###");

                        for (int j = 0; j < forOneWord.length; j++) {

                            String[] suggAndWeight = forOneWord[j].split("@");

                            if (! context.getRunningQuery().getQueryString().equalsIgnoreCase(suggAndWeight[0])) {

                                RelevantQuery rq = new RelevantQuery(suggAndWeight[0], Integer.valueOf(suggAndWeight[1]));
                                searchResult.addRelevantQuery(rq);
                            }
                        }
                    }
                }
            }
        }
    }



    // Inner classes -------------------------------------------------

}
