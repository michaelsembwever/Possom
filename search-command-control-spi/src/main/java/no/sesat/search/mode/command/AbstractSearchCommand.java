/* Copyright (2006-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.mode.command;


import no.sesat.search.mode.command.querybuilder.BaseFilterBuilder;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Collections;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.mode.config.BaseSearchConfiguration;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.Query;
import no.sesat.commons.visitor.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.AbstractQueryParserContext;
import no.sesat.commons.visitor.AbstractReflectionVisitor;
import no.sesat.search.query.parser.QueryParser;
import no.sesat.search.query.parser.QueryParserImpl;
import no.sesat.search.query.parser.TokenMgrError;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenEvaluationEngineImpl;
import no.sesat.search.query.token.TokenPredicate;
import no.sesat.search.query.transform.QueryTransformer;
import no.sesat.search.query.transform.QueryTransformerConfig;
import no.sesat.search.query.transform.QueryTransformerFactory;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.result.handler.DataModelResultHandler;
import no.sesat.search.result.handler.ResultHandler;
import no.sesat.search.result.handler.ResultHandlerConfig;
import no.sesat.search.result.handler.ResultHandlerFactory;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.view.config.SearchTab;
import static no.sesat.search.view.navigation.NavigationConfig.USER_SORT_KEY;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import no.sesat.search.datamodel.access.DataModelAccessException;
import no.sesat.search.mode.command.querybuilder.FilterBuilder;
import no.sesat.search.mode.command.querybuilder.QueryBuilder;
import no.sesat.search.mode.command.querybuilder.SesamSyntaxQueryBuilder;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig.Controller;
import no.sesat.search.query.token.TokenPredicateUtility;
import no.sesat.search.site.config.SiteClassLoaderFactory;
import no.sesat.search.site.config.Spi;
import no.sesat.search.view.navigation.NavigationConfig.Nav;

/** The base abstraction for Search Commands providing a large framework for commands to run against.
 *                                                                                                          <br/><br/>
 * While the SearchCommand interface defines basic execution behavour this abstraction defines:<ul>
 * <li>delegation of the call method to the execute method so to provide a default implementation for handling
 *      cancellations, thread renaming during execution, and avoidance of execution on blank queries,
 * <li>assigned queryBuilder and filterBuilder to express the query and filter as the index's requires,</li>
 * <li>delegation to the appropriate query to use (sometimes not the user's query),</li>
 * <li>handling and control of the query transformations as defined in the commands config,</li>
 * <li>handling and control of the result handlers as defined in the commands config,</li>
 * <li>helper methods, beyond the query transformers, for filter (and advanced-filter) construction,</li>
 * <li>assigned displayableQueryBuilder for constructing a user presentable version of the transformed query,
 *       that in turn can be parsed again by sesat's query parser to return the same query.</li>
 * </ul>
 *                                        <br/><br/>
 *
 * This command undertook a large refactoring in 2.18 to clean up internal concerns.
 * See the specification {@link http://sesat.no/new-design-proposal-for-searchcommand-and-abstractsearchcommand.html}
 *
 * @version <tt>$Id$</tt>
 */
public abstract class AbstractSearchCommand implements SearchCommand, Serializable {

    // Constants -----------------------------------------------------

    private static final DataModelResultHandler DATAMODEL_HANDLER = new DataModelResultHandler();

    private static final Logger LOG = Logger.getLogger(AbstractSearchCommand.class);
    protected static final Logger DUMP = Logger.getLogger("no.sesat.search.Dump");

    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";
    private static final String ERR_TRANSFORMED_QUERY_USED
            = "Cannot use transformedTerms Map once deprecated getTransformedQuery as been used";
    private static final String ERR_HANDLING_CANCELLATION
            = "Cancellation (and now handling of) occurred to ";
    private static final String ERROR_RUNTIME = "RuntimeException occurred";
    private static final String TRACE_NOT_TOKEN_PREDICATE = "Not a TokenPredicate ";

    // Attributes ----------------------------------------------------

    /**
     * The context to work against.
     */
    protected transient final Context context;
    private transient Query query = null;
    private transient TokenEvaluationEngine engine = null;
    private transient final QueryTransformerFactory.Context qtfContext;
    private transient final QueryBuilder.Context queryBuilderContext;
    private transient final QueryTransformer initialQueryTransformer;
    private transient final QueryBuilder queryBuilder;
    private transient final SesamSyntaxQueryBuilder displayableQueryBuilder;
    private transient final FilterBuilder filterBuilder;

    protected final String untransformedQuery;
    private final Map<Clause, String> transformedTerms = new LinkedHashMap<Clause, String>();
    private String transformedQuery;
    private String transformedQuerySesamSyntax;

    private final SearchCommandParameter offsetParameter;
    private final SearchCommandParameter userSortByParameter;

    protected transient final DataModel datamodel;

    protected volatile boolean completed = false;
    private volatile Thread thread = null;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Default constructor. Only constructor.
     * @param cxt The context to execute in.
     */
    public AbstractSearchCommand(final SearchCommand.Context cxt) {

        LOG.trace("AbstractSearchCommand()");


        assert null != cxt.getDataModel() : "Not allowed to pass in null datamodel";
        assert null != cxt.getDataModel().getQuery() : "Not allowed to pass in null datamodel.query";

        this.context = cxt;
        this.datamodel = cxt.getDataModel();

        // do not use this.getSearchConfiguration() in constructor -- it's a overridable method.
        final BaseSearchConfiguration bsc = (BaseSearchConfiguration) context.getSearchConfiguration();

        initialiseQuery();
        // getQuery() may be overridden so we need to be careful here
        transformedQuery = getQuery().getQueryString();

        // A simple context for QueryTransformerFactory.Context
        qtfContext = new QueryTransformerFactory.Context() {
                public Site getSite() {
                    return context.getDataModel().getSite().getSite();
                }
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
            };

        // Little more complicated context for QueryBuilder.Context (can be used for QueryTransformer.Context too)
        queryBuilderContext = ContextWrapper.wrap(
                QueryBuilder.Context.class,
                new BaseContext(){
                    public Site getSite() {
                        return datamodel.getSite().getSite();
                    }

                    public String getTransformedQuery() {
                        return transformedQuery;
                    }

                    public Query getQuery() {
                        // Important that initialiseQuery() has been called first
                        return getSearchCommandsQuery();
                    }

                    public TokenEvaluationEngine getTokenEvaluationEngine() {
                        return engine;
                    }

                    public void visitXorClause(final Visitor visitor, final XorClause clause) {
                        searchCommandsVisitXorClause(visitor, clause);
                    }

                    public String getFieldFilter(final LeafClause clause) {
                        return getSearchCommandsFieldFilter(clause);
                    }

                    public String getTransformedTerm(final Clause clause) {

                        // unable to delegate to getTransformedTerm as it escapes reserved words
                        //  and we're not allowed to here
                        final String transformedTerm = transformedTerms.get(clause);
                        return null != transformedTerm ? transformedTerm : clause.getTerm();
                    }

                    public Collection<String> getReservedWords() {

                        return getSearchCommandsReservedWords();
                    }

                    public String escape(final String word) {

                        return searchCommandsEscape(word);
                    }

                    public Map<Clause, String> getTransformedTerms() {
                        return getSearchCommandsTransformedTerms();
                    }
                },
                cxt
            );

        // initialise the transformed terms
        initialQueryTransformer = new QueryTransformerFactory(qtfContext)
                .getController(bsc.getInitialQueryTransformer());
        initialQueryTransformer.setContext(queryBuilderContext);
        initialiseTransformedTerms();

        // construct the queryBuilder
        queryBuilder = constructQueryBuilder(cxt, queryBuilderContext);

        // construct the sesamSyntaxQueryBuilder
        displayableQueryBuilder = new SesamSyntaxQueryBuilder(queryBuilderContext, bsc);

        // FIXME implement configuration lookup
        filterBuilder = new BaseFilterBuilder(queryBuilderContext, null);

        // run an initial queryBuilder run and store the untransformed resulting queryString.
        untransformedQuery = getQueryRepresentation();

        // parameters

        offsetParameter = new NavigationSearchCommandParameter(
                context,
                getSearchConfiguration().getPagingParameter(),
                getSearchConfiguration().getPagingParameter(),
                BaseSearchCommandParameter.Origin.REQUEST);

        userSortByParameter = new NavigationSearchCommandParameter(
                context,
                getSearchConfiguration().getUserSortParameter(),
                getSearchConfiguration().getUserSortParameter(),
                BaseSearchCommandParameter.Origin.REQUEST);

    }

    /** Set (or reset) the transformed terms back to the state before any queryTransformers were run.
     */
    protected final void initialiseTransformedTerms(){

        initialQueryTransformer.visit(query.getRootClause());
    }

    // Public --------------------------------------------------------

    public abstract ResultList<ResultItem> execute();

    /**
     * Use this always instead of datamodel.getQuery().getQuery()
     * because the command could be running off a different query string.
     *
     * @return
     */
    public Query getQuery() {

        return query;
    }

    /**
     * Returns the query as it is after the query transformers and command specific query builder
     * have been applied to it.
     *
     * @return The transformed query.
     */
    public String getTransformedQuery() {

        return transformedQuery;
    }

    @Override
    public String toString() {
        return getSearchConfiguration().getId() + ' ' + datamodel.getQuery().getString();
    }

    // SearchCommand overrides ---------------------------------------------------

    public BaseSearchConfiguration getSearchConfiguration() {
        return (BaseSearchConfiguration) context.getSearchConfiguration();
    }

    /**
     * Called by thread executor
     *
     * @return
     */
    public ResultList<ResultItem> call() {

        MDC.put(Site.NAME_KEY, datamodel.getSite().getSite().getName());
        MDC.put("UNIQUE_ID", datamodel.getParameters().getUniqueId());
        thread = Thread.currentThread();

        final String t = thread.getName();
        final String statName = getSearchConfiguration().getStatisticalName();

        if (statName != null && statName.length() > 0) {
            Thread.currentThread().setName(t + " [" + getSearchConfiguration().getStatisticalName() + ']');
        } else {
            Thread.currentThread().setName(t + " [" + getClass().getSimpleName() + ']');
        }

        try {
            try {

                LOG.trace("call()");

                performQueryTransformation();
                checkForCancellation();

                final ResultList<ResultItem> result = performExecution();
                checkForCancellation();

                performResultHandling(result);
                checkForCancellation();

                completed = true;
                thread = null;
                return result;

            } catch(UndeclaredThrowableException ute){

                if(ute.getCause() instanceof DataModelAccessException && isCancelled()){

                    // This is partially expected because the datamodel's
                    //  controlLevel would have moved on through the process stack.
                    LOG.trace("Cancelled command threw underlying exception", ute.getCause());
                    return new BasicResultList<ResultItem>();

                }
                throw ute;
            }

        } catch (RuntimeException rte) {
            LOG.error(ERROR_RUNTIME, rte);
            return new BasicResultList<ResultItem>();

        } finally {
            // restore thread name
            Thread.currentThread().setName(t);
        }
    }

    /**
     * Handles cancelling the command.
     *  Inserts an "-1" result list. And does the result handling on it.
     * Returns true if cancellation action was taken.
     */
    public synchronized boolean handleCancellation() {

        if (!completed) {
            LOG.error(ERR_HANDLING_CANCELLATION
                    + getSearchConfiguration().getId()
                    + " [" + getClass().getSimpleName() + ']');

            if (null != thread) {
                thread.interrupt();
                thread = null;
            }
            performResultHandling(new BasicResultList<ResultItem>());
        }
        return !completed;
    }

    /** Has the command been cancelled.
     * Calling this method only makes sense once the call() method has been.
     **/
    public synchronized boolean isCancelled(){
        return null == thread && !completed;
    }

    // Protected -----------------------------------------------------

    /** Construct from scratch, and return the query builder to use.
     * Default implementation returns the query builder that is configured from the BaseSearchConfiguration.
     *
     * <br/>
     *
     * This method is intended to be overridden, but it called from the constructor.
     * So it is important the overrides do not reference "this",
     *  or any other fields as they will likely not be initialised yet.
     *
     * @param cxt search command's context
     * @param queryBuilderContext the query builder context
     * @return
     */
    protected QueryBuilder constructQueryBuilder(
            final SearchCommand.Context cxt,
            final QueryBuilder.Context queryBuilderContext){

        return QueryBuilderFactory.getController(
                queryBuilderContext,
                ((BaseSearchConfiguration)cxt.getSearchConfiguration()).getQueryBuilder());
    }

    protected Collection<String> getReservedWords(){
        return Collections.emptySet();
    }

    /**
     * @param visitor
     * @param clause
     */
    protected void visitXorClause(final Visitor visitor, final XorClause clause) {

        // determine which branch in the query-tree we want to use.
        //  Both branches to a XorClause should never be used.
        switch (clause.getHint()) {
            default:
                clause.getFirstClause().accept(visitor);
                break;
        }
    }

    /** Get the results from another search command waiting if neccessary.
     * @param id
     * @param datamodel
     * @return
     * @throws java.lang.InterruptedException
     */
    protected final ResultList<ResultItem> getSearchResult(
            final String id,
            final DataModel datamodel) throws InterruptedException {

        synchronized (datamodel.getSearches()) {
            while (null == datamodel.getSearch(id)) {
                // we're not going to hang around waiting if we've been already left out in the cold
                checkForCancellation();
                // next line releases the monitor so it is possible to call this method from different threads
                datamodel.getSearches().wait(1000);
            }
        }
        return datamodel.getSearch(id).getResults();
    }

    protected void performQueryTransformation() {

        applyQueryTransformers(
                getQuery(),
                getSearchConfiguration().getQueryTransformers());
    }

    /** Handles the execution process. Will determine whether to call execute() and wrap it with timing info.
     * @return
     */
    protected final ResultList<ResultItem> performExecution() {

        final StopWatch watch = new StopWatch();
        watch.start();

        final String notNullQuery = null != getTransformedQuery() ? getTransformedQuery().trim() : "";
        Integer hitCount = null;

        try {

            // we will be executing the command IF there's a valid query or filter,
            // or if the configuration specifies that we should run anyway.
            boolean executeQuery = null != datamodel.getQuery() && "*".equals(datamodel.getQuery().getString());
            executeQuery |= notNullQuery.length() > 0 || getSearchConfiguration().isRunBlank();
            executeQuery |= null != getFilter() && 0 < getFilter().length();

            LOG.info("executeQuery==" + executeQuery + " ; query:" + notNullQuery + " ; filter:" + getFilter());

            final ResultList<ResultItem> result = executeQuery
                    ? execute()
                    : new BasicResultList<ResultItem>();

            if(!executeQuery){
                // sent hit count to zero since we have intentionally avoiding searching.
                result.setHitCount(0);
            }

            hitCount = result.getHitCount();

            LOG.debug("Hits is " + getSearchConfiguration().getId() + ':' + hitCount);

            return result;

        } finally {

            watch.stop();
            LOG.info("Search " + getSearchConfiguration().getId() + " took " + watch);

            statisticsInfo(
                    "<search-command id=\"" + getSearchConfiguration().getId()
                            + "\" name=\"" + getSearchConfiguration().getStatisticalName()
                            + "\" type=\"" + getClass().getSimpleName() + "\">"
                            + (hitCount != null ? "<hits>" + hitCount + "</hits>" : "<failure/>")
                            + "<time>" + watch + "</time>"
                            + "</search-command>");
        }
    }

    /**
     * Perform (delegating out to) all registered result handlers for this command.
     * Also performs some hardcoded result handling, eg DataModelResultHandler.
     *
     * @param result
     */
    protected final void performResultHandling(final ResultList<ResultItem> result) {

        // Build the context each result handler will need.
        final ResultHandler.Context resultHandlerContext = ContextWrapper.wrap(
                ResultHandler.Context.class,
                new BaseContext() {
                    public Site getSite(){
                        return context.getDataModel().getSite().getSite();
                    }
                    public ResultList<ResultItem> getSearchResult() {
                        return result;
                    }
                    public SearchTab getSearchTab() {
                        return datamodel.getPage().getCurrentTab();
                    }
                    public Query getQuery() {
                        return getSearchCommandsQuery();
                    }
                    public String getDisplayQuery(){
                        return getTransformedQuerySesamSyntax();
                    }
                },
                context
        );

        // process listed result handlers
        for (ResultHandlerConfig resultHandlerConfig : getSearchConfiguration().getResultHandlers()) {

            ResultHandlerFactory.getController(resultHandlerContext, resultHandlerConfig)
                    .handleResult(resultHandlerContext, datamodel);
        }

        // The DataModel result handler is a hardcoded feature of the architecture
        DATAMODEL_HANDLER.handleResult(resultHandlerContext, datamodel);

    }

    /**
     * Returns the offset in the result set. If paging is enabled for the
     * current search configuration the offset to the current page will be
     * added to the parameter.
     *
     * @param i the current offset.
     * @return i plus the offset of the current page.
     *
     * @deprecated instead use getOffset() + i
     */
    protected int getCurrentOffset(final int i) {

        return i + getOffset();
    }

    /**
     * Returns the offset applicable to this command.
     * Zero if the command has no "offset" navigator configured,
     *  the value of the offset parameter otherwise.
     *
     * @return the offset.
     */
    protected int getOffset(){

        return null != offsetParameter.getValue()
                ? Integer.parseInt(offsetParameter.getValue())
                : 0;
    }

    public boolean isPaginated(){

        return offsetParameter.isActive();
    }

   /**
    * Returns the userSortBy applicable to this command and request.
    * Null if the command has no "sort" navigator configured,
    *  the value of the user's userSortBy parameter.
    *
    * This method does not return any command configuration's sort-by attribute (as some subclasses have).
    *
    * @return the userSortBy. returns null when false == isUserSortable().
    */
    protected String getUserSortBy(){

        return userSortByParameter.getValue();
    }

    public boolean isUserSortable(){

        return userSortByParameter.isActive();
    }

    /**
     * Returns parameter value.
     *  Changed since 2.16.1 so that only request parameters are searched.
     *
     * @param paramName the name of the parameter to look for.
     * @return the parameter value, unescaped, or null if parameter does not exist.
     */
    protected String getParameter(final String paramName) {

        final Map<String, StringDataObject> parameters = datamodel.getParameters().getValues();
        return parameters.containsKey(paramName) ? parameters.get(paramName).getString() : null;
    }

    // <-- Query Representation state methods (useful while the inbuilt visitor is in operation)

    protected QueryBuilder getQueryBuilder(){
        return queryBuilder;
    }

    protected synchronized String getQueryRepresentation() {

        return getQueryBuilder().getQueryString();
    }

    protected FilterBuilder getFilterBuilder(){
        return filterBuilder;
    }

    /**
     * @todo rename to getFilterString
     *
     * @return
     */
    protected String getFilter() {
        return filterBuilder.getFilterString();
    }

    // Query Representation state methods -->

    protected final Map<Clause, String> getTransformedTerms() {
        return transformedTerms;
    }

    /** Get a string parameter (first if array exists).
     *
     * @param paramName parameter name
     * @return null when array is null
     */
    protected final String getSingleParameter(final String paramName) {

        final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
        return parameters.get(paramName) instanceof String[]
                ? ((String[]) parameters.get(paramName))[0]
                : (String) parameters.get(paramName);
    }

    /**
     * Use this always instead of context.getTokenEvaluationEngine()
     * because the command could be running off a different query string.
     *
     * @return
     */
    protected TokenEvaluationEngine getEngine() {

        return engine;
    }

    /**
     * XXX Very expensive method to call!
     *
     * @param queryString
     * @return
     */
    protected final ReconstructedQuery createQuery(final String queryString) {

        LOG.debug("createQuery(" + queryString + ')');

        if (datamodel.getQuery().getQuery().getQueryString().equalsIgnoreCase(queryString)) {

            // return original query and engine
            return new ReconstructedQuery(datamodel.getQuery().getQuery(), context.getTokenEvaluationEngine());

        } else {

            final TokenEvaluationEngine.Context tokenEvalFactoryCxt = ContextWrapper.wrap(
                    TokenEvaluationEngine.Context.class,
                    context,
                    new BaseContext() {
                        public String getQueryString() {
                            return queryString;
                        }
                        public Site getSite() {
                            return datamodel.getSite().getSite();
                        }
                        public String getUniqueId(){
                            return datamodel.getParameters().getUniqueId();
                        }
                    }
            );

            // This will among other things perform the initial fast search
            // for textual analysis.
            final TokenEvaluationEngine engine = new TokenEvaluationEngineImpl(tokenEvalFactoryCxt);

            // queryStr parser
            final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
                public TokenEvaluationEngine getTokenEvaluationEngine() {
                    return engine;
                }
            });

            try {
                return new ReconstructedQuery(parser.getQuery(), engine);

            } catch (TokenMgrError ex) {
                // Errors (as opposed to exceptions) are fatal.
                LOG.fatal(ERR_PARSING, ex);
            }
        }
        return null;
    }


    /**
     * Escape the word (whether it requires escaping or not).
     *
     * Default escaping for strings is to enclose in quotes, ie to phrase the word.
     * Default escaping for the ':' character is "\\:".
     *
     * Override this to match back-end (index) specific escaping.
     *
     * @param word The term to escape
     * @return The escaped version of term.
     */
    protected String escape(final String word) {

        if(":".equals(word)){
            return "\\:";
        }else{
            return '"' + word + '"';
        }
    }

    /**
     * Returns null when no field exists.
     * @param clause
     * @return
     */
    protected final String getFieldFilter(final LeafClause clause) {

        String field = null;
        if (null != clause.getField()) {
            final Map<String, String> fieldFilters = getSearchConfiguration().getFieldFilterMap();
            if (fieldFilters.containsKey(clause.getField())) {
                field = clause.getField();
            } else {

                for (String fieldFilter : fieldFilters.keySet()) {
                    try {
                        final TokenPredicate tp = TokenPredicateUtility.getTokenPredicate(fieldFilter);
                        // if the field is the token then mask the field and include the term.
                        // XXX why are we checking the known and possible predicates?
                        boolean result = clause.getKnownPredicates().contains(tp);

                        result |= clause.getPossiblePredicates().contains(tp)
                                && getEngine().evaluateTerm(tp, clause.getField());

                        if (result) {
                            field = fieldFilter;
                            break;
                        }
                    } catch (IllegalArgumentException iae) {
                        LOG.trace(TRACE_NOT_TOKEN_PREDICATE + fieldFilter);
                    }
                }
            }
        }
        return field;
    }

    protected final void statisticsInfo(final String msg) {

        final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
        final StringBuffer logger = (StringBuffer) parameters.get("no.sesat.Statistics");
        if (null != logger) {
            logger.append(msg);
        }
    }

    /**
     * Returns the query as it is after the query transformers have been applied to it.
     *
     * It is normalised.
     *
     * @return
     */
    protected String getTransformedQuerySesamSyntax() {

        // if it's been nulled then return the original query
        return null != transformedQuerySesamSyntax
                ? transformedQuerySesamSyntax.replaceAll(" +", " ") // also normalise it
                : context.getDataModel().getQuery().getString();
    }

    protected void updateTransformedQuerySesamSyntax(){

        setTransformedQuerySesamSyntax(displayableQueryBuilder.getQueryString());
    }

    protected void setTransformedQuerySesamSyntax(final String sesamSyntax){

        transformedQuerySesamSyntax = sesamSyntax;
    }


    // Private -------------------------------------------------------

    private void initialiseQuery() {

        // use the query or something search-command specific
        final String queryParameter = getSearchConfiguration().getQueryParameter();

        if (queryParameter != null && queryParameter.length() > 0) {
            // It's not the query we are looking for but a string held in a different parameter.
            final StringDataObject queryToUse = datamodel.getParameters().getValue(queryParameter);
            if (queryToUse != null) {
                final ReconstructedQuery recon = createQuery(queryToUse.getString());
                query = recon.getQuery();
                engine = recon.getEngine();
                return;
            }
        }
        query = datamodel.getQuery().getQuery();
        engine = context.getTokenEvaluationEngine();
    }

    private void applyQueryTransformers(
            final Query query,
            final List<QueryTransformerConfig> transformers) {

        if (transformers != null && transformers.size() > 0) {
            boolean touchedTransformedQuery = false;

            final QueryTransformerFactory queryTransformerFactory = new QueryTransformerFactory(qtfContext);

            for (QueryTransformerConfig transformerConfig : transformers) {

                final QueryTransformer transformer = queryTransformerFactory.getController(transformerConfig);

                final boolean ttq = touchedTransformedQuery;

                final QueryTransformer.Context qtCxt = ContextWrapper.wrap(
                        QueryTransformer.Context.class,
                        new BaseContext() {
                            public Map<Clause, String> getTransformedTerms() {
                                if (ttq) {
                                    throw new IllegalStateException(ERR_TRANSFORMED_QUERY_USED);
                                }
                                return transformedTerms;
                            }
                        },
                        queryBuilderContext);

                transformer.setContext(qtCxt);

                final String newTransformedQuery = transformer.getTransformedQuery();
                touchedTransformedQuery |= (!transformedQuery.equals(newTransformedQuery));

                if (touchedTransformedQuery) {
                    transformedQuery = newTransformedQuery;
                } else {

                    transformer.visit(query.getRootClause());
                    transformedQuery = getQueryRepresentation();
                }

                addFilterString(transformer.getFilter(datamodel.getJunkYard().getValues()));
                addFilterString(transformer.getFilter());


                LOG.debug(transformer.getClass().getSimpleName() + "--> TransformedQuery=" + transformedQuery);
                LOG.debug(transformer.getClass().getSimpleName() + "--> Filter=" + filterBuilder.getFilterString());
            }

        } else {
            transformedQuery = getQueryRepresentation();
        }

        updateTransformedQuerySesamSyntax();
    }

    /** Makes presumption that filter is in format "field:value".
     * Must be overridden if QueryTransformers return filters in an alternative format.
     *
     * @param filter
     */
    protected void addFilterString(final String filter){
        if(null != filter && filter.length() > 0){
            final int pos = filter.indexOf(":");

            if(pos > 0){
                filterBuilder.addFilter(filter.substring(0, pos), filter.substring(pos+1));
            }else{
                filterBuilder.addFilter(null, filter);
            }
        }
    }

    /** If the command has been cancelled will throw the appropriate SearchCommandException.
     * Calling this method only makes sense once the call() method has been,
     *   otherwise it is guaranteed to throw the exception.
     * @throws SearchCommandException when cancellation has occurred.
     **/
    private void checkForCancellation() throws SearchCommandException{
        if( isCancelled() ){ throw new SearchCommandException("cancelled", new InterruptedException()); }
    }

    /** Wrapper around getQuery(). Nothing more than a different method signature. **/
    private Query getSearchCommandsQuery(){
        return getQuery();
    }

    /** Wrapper around visitXorClause(). Nothing more than a different method signature. **/
    private void searchCommandsVisitXorClause(final Visitor visitor, final XorClause clause) {
        visitXorClause(visitor, clause);
    }

    /** Wrapper around getReservedWords(). Nothing more than a different method signature. **/
    private String getSearchCommandsFieldFilter(final LeafClause clause) { return getFieldFilter(clause); }

    /** Wrapper around getQuery(). Nothing more than a different method signature. **/
    private Collection<String> getSearchCommandsReservedWords() { return getReservedWords(); }

    /** Wrapper around escape(). Nothing more than a different method signature. **/
    private String searchCommandsEscape(final String word) { return escape(word); }

    /** Wrapper around getTransformedTerms(). Nothing more than a different method signature. **/
    private Map<Clause, String> getSearchCommandsTransformedTerms() { return getTransformedTerms(); }

    /**
     *
     * @return
     */
    protected int getResultsToReturn() {
        return context.getSearchConfiguration().getResultsToReturn();
    }

    // Inner classes -------------------------------------------------

    /**
     * see createQuery(string)
     */
    protected static class ReconstructedQuery {
        private final Query query;
        private final TokenEvaluationEngine engine;

        ReconstructedQuery(final Query query, final TokenEvaluationEngine engine) {
            this.query = query;
            this.engine = engine;
        }

        /**
         * @return
         */
        public Query getQuery() {
            return query;
        }

        /**
         * @return
         */
        public TokenEvaluationEngine getEngine() {
            return engine;
        }
    }

    protected static final class QueryBuilderFactory {

        // Constants -----------------------------------------------------


        // Attributes ----------------------------------------------------

        // Static --------------------------------------------------------


        // Constructors --------------------------------------------------

        /** Creates a new instance of QueryBuilderFactory */
        private QueryBuilderFactory() {
        }

        // Public --------------------------------------------------------


        /**
         *
         * @param context
         * @param config
         * @return
         */
        public static QueryBuilder getController(
                final QueryBuilder.Context context,
                final QueryBuilderConfig config){

            final String name = "no.sesat.search.mode.command.querybuilder."
                    + config.getClass().getAnnotation(Controller.class).value();

            try{

                final SiteClassLoaderFactory.Context ctlContext = ContextWrapper.wrap(
                        SiteClassLoaderFactory.Context.class,
                        new BaseContext() {
                            public Spi getSpi() {
                                return Spi.SEARCH_COMMAND_CONTROL;
                            }
                        },
                        context
                    );

                final ClassLoader ctlLoader = SiteClassLoaderFactory.instanceOf(ctlContext).getClassLoader();

                @SuppressWarnings("unchecked")
                final Class<? extends QueryBuilder> cls = (Class<? extends QueryBuilder>)ctlLoader.loadClass(name);

                final Constructor<? extends QueryBuilder> constructor
                        = cls.getConstructor(QueryBuilder.Context.class, QueryBuilderConfig.class);

                return constructor.newInstance(context, config);

            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException(ex);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException(ex);
            } catch (InvocationTargetException ex) {
                throw new IllegalArgumentException(ex);
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException(ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        // Package protected ---------------------------------------------

        // Protected -----------------------------------------------------

        // Private -------------------------------------------------------

        // Inner classes -------------------------------------------------

    }
}
