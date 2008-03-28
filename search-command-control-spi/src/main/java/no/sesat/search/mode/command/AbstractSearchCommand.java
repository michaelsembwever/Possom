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


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.PhraseClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.AbstractQueryParserContext;
import no.sesat.search.query.parser.AbstractReflectionVisitor;
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
import static no.sesat.search.view.navigation.ResultPagingNavigationConfig.OFFSET_KEY;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import no.sesat.search.datamodel.access.DataModelAccessException;
import no.sesat.search.view.navigation.NavigationConfig.Nav;

/** The base abstraction for Search Commands providing a large framework for commands to run against.
 *                                                                                                          <br/><br/>
 * While the SearchCommand interface defines basic execution behavour this abstraction defines:<ul>
 * <li>delegation of the call method to the execute method so to provide a default implementation for handling
 *      cancellations, thread renaming during execution, and avoidance of execution on blank queries,
 * <li>internal visitor pattern to express the query string in the index's required manner,</li>
 * <li>helper methods for the internal visitor pattern,</li>
 * <li>delegation to the appropriate query to use (sometimes not the user's query),</li>
 * <li>handling and control of the query transformations as defined in the commands config,</li>
 * <li>handling and control of the result handlers as defined in the commands config,</li>
 * <li>helper methods, beyond the query transformers, for filter (and advanced-filter) construction,</li>
 * <li>basic implementation (visitor pattern) for constructing a user presentable version of the transformed query.</li>
 * </ul>
 *                                                                                                          <br/><br/>
 * 
 * <b>TODO</b> There is work planned to separate and encapsulate alot of this functionality into individual classes. 
 *                   https://jira.sesam.no/jira/browse/SEARCH-2149                                            <br/><br/>
 * 
 * 
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>.
 * @version <tt>$Id$</tt>
 */
public abstract class AbstractSearchCommand extends AbstractReflectionVisitor implements SearchCommand, Serializable {

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

    /**
     *
     */
    protected final String untransformedQuery;
    private String filter = "";
    private final String additionalFilter;
    private final Map<Clause, String> transformedTerms = new LinkedHashMap<Clause, String>();
    private String transformedQuery;
    private String transformedQuerySesamSyntax;
    /**
     *
     */
    protected transient final DataModel datamodel;
    /**
     *
     */
    protected volatile boolean completed = false;
    private volatile Thread thread = null;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * @param cxt The context to execute in.
     */
    public AbstractSearchCommand(final SearchCommand.Context cxt) {

        LOG.trace("AbstractSearchCommand()");


        assert null != cxt.getDataModel() : "Not allowed to pass in null datamodel";
        assert null != cxt.getDataModel().getQuery() : "Not allowed to pass in null datamodel.query";

        this.context = cxt;
        this.datamodel = cxt.getDataModel();

        initialiseQuery();
        transformedQuery = query.getQueryString();

        untransformedQuery = initialiseTransformedTerms(query);

        // create additional filters
        final FilterVisitor additionalFilterVisitor = new FilterVisitor();
        additionalFilterVisitor.visit(query.getRootClause());
        additionalFilter = additionalFilterVisitor.getFilter();
    }

    // Public --------------------------------------------------------


    /**
     * TODO comment me.
     *
     * @return
     */
    public abstract ResultList<? extends ResultItem> execute();

    /**
     * Returns the query as it is after the query transformers and command specific query builder
     * have been applied to it.
     *
     * @return The transformed query.
     */
    public String getTransformedQuery() {

        return transformedQuery;
    }

    /**
     * TODO comment me. *
     */
    @Override
    public String toString() {
        return getSearchConfiguration().getName() + ' ' + datamodel.getQuery().getString();
    }

    /**
     * TODO comment me. *
     *
     * @return
     */
    public String getFilter() {
        return filter;
    }

    // SearchCommand overrides ---------------------------------------------------

    /**
     * TODO comment me. *
     */
    public SearchConfiguration getSearchConfiguration() {
        return context.getSearchConfiguration();
    }

    /**
     * Called by thread executor
     *
     * @return
     */
    public ResultList<? extends ResultItem> call() {

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

                final ResultList<? extends ResultItem> result = performExecution();
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
                    + getSearchConfiguration().getName()
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

    // AbstractReflectionVisitor overrides ----------------------------------------------

    private final StringBuilder sb = new StringBuilder(128);

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected void visitImpl(final LeafClause clause) {

        appendToQueryRepresentation(getTransformedTerm(clause));

//        if (null == getTransformedTerm(clause)) {
//            if (null != clause.getField()) {
//                if (null == getFieldFilter(clause)) {
//
//                    // Escape any fielded leafs for fields that are not supported by this command.
//                    appendToQueryRepresentation(getTransformedTerm(clause));
//
//                }
//            } else {
//
//                appendToQueryRepresentation(getTransformedTerm(clause));
//            }
//        }
    }

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" AND ");
        clause.getSecondClause().accept(this);
    }

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected void visitImpl(final OrClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" OR ");
        clause.getSecondClause().accept(this);
    }

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(" ");
        clause.getSecondClause().accept(this);
    }

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected void visitImpl(final NotClause clause) {
        final String childsTerm = transformedTerms.get(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("NOT ");
            clause.getFirstClause().accept(this);
        }
    }

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected void visitImpl(final AndNotClause clause) {
        final String childsTerm = transformedTerms.get(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            appendToQueryRepresentation("ANDNOT ");
            clause.getFirstClause().accept(this);
        }
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

    /**
     * TODO comment me. *
     *
     * @param clause
     */
    protected final void visitImpl(final XorClause clause) {
        visitXorClause(this, clause);
    }

    // Protected -----------------------------------------------------

    /** Get the results from another search command waiting if neccessary.
     * @param id 
     * @param datamodel 
     * @return
     * @throws java.lang.InterruptedException 
     */
    protected final ResultList<? extends ResultItem> getSearchResult(
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

    /**
     * Returns the leaf representation with the special characters making it a fielded leaf escaped.
     *
     * @param clause The fielded leaf to escape.
     * @return The escaped string representation of the leaf.
     */
    protected String escapeFieldedLeaf(final LeafClause clause) {
        return clause.getField() + "\\:" + clause.getTerm();
    }

    /**
     * TODO comment me.
     */
    protected void performQueryTransformation() {

        applyQueryTransformers(
                getQuery(),
                getEngine(),
                getSearchConfiguration().getQueryTransformers());
    }


    /** Handles the execution process. Will determine whether to call execute() and wrap it with timing info.
     * @return
     */
    protected final ResultList<? extends ResultItem> performExecution() {

        final StopWatch watch = new StopWatch();
        watch.start();

        final String notNullQuery = null != getTransformedQuery() ? getTransformedQuery().trim() : "";
        Integer hitCount = null;

        try {

            // we will be executing the command IF there's a valid query or filter,
            // or if the configuration specifies that we should run anyway.
            boolean executeQuery = null != datamodel.getQuery() && "*".equals(datamodel.getQuery().getString());
            executeQuery |= notNullQuery.length() > 0 || getSearchConfiguration().isRunBlank();
            executeQuery |= null != filter && 0 < filter.length();
            executeQuery |= null != additionalFilter && 0 < additionalFilter.length();

            LOG.info("executeQuery==" + executeQuery + " ; query:" + notNullQuery + " ; filter:" + filter);

            final ResultList<? extends ResultItem> result = executeQuery
                    ? execute()
                    : new BasicResultList<ResultItem>();

            if(!executeQuery){
                // sent hit count to zero since we have intentionally avoiding searching.
                result.setHitCount(0);
            }

            hitCount = result.getHitCount();

            LOG.debug("Hits is " + getSearchConfiguration().getName() + ':' + hitCount);

            return result;

        } finally {

            watch.stop();
            LOG.info("Search " + getSearchConfiguration().getName() + " took " + watch);

            statisticsInfo(
                    "<search-command id=\"" + getSearchConfiguration().getName()
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
    protected final void performResultHandling(final ResultList<? extends ResultItem> result) {

        // Build the context each result handler will need.
        final ResultHandler.Context resultHandlerContext = ContextWrapper.wrap(
                ResultHandler.Context.class,
                new BaseContext() {
                    public Site getSite(){
                        return context.getDataModel().getSite().getSite();
                    }
                    public ResultList<? extends ResultItem> getSearchResult() {
                        return result;
                    }
                    public SearchTab getSearchTab() {
                        return datamodel.getPage().getCurrentTab();
                    }
                    public Query getQuery() {
                        return AbstractSearchCommand.this.getQuery();
                    }
                    public String getDisplayQuery(){
                        return AbstractSearchCommand.this.getTransformedQuerySesamSyntax();
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
     * @return i plus the offset of the current page.
     */    
    protected int getOffset(){
        
        int offset = 0;
        
        if(isPaginated()){
            final StringDataObject offsetString = context.getDataModel().getParameters().getValue(OFFSET_KEY);
            if( null != offsetString ){
                offset = Integer.parseInt(offsetString.getUtf8UrlEncoded());
            }
        }
        return offset;
    }
    
    public boolean isPaginated(){
        
        final boolean navMapExists = null != context.getDataModel().getNavigation()
                && null != context.getDataModel().getNavigation().getConfiguration();
        
        final Nav offsetNav = navMapExists
                ? context.getDataModel().getNavigation().getConfiguration().getNavMap().get(OFFSET_KEY)
                : null;
        
        return null != offsetNav && getSearchConfiguration().getName().equals(offsetNav.getCommandName());
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
    //  -    TODO enscapsulated this state in a separate inner class.

    /**
     * TODO comment me. *
     */
    protected synchronized String getQueryRepresentation(final Query query) {

        final Clause root = query.getRootClause();
        sb.setLength(0);
        visit(root);
        return sb.toString().trim();
    }

    /**
     * TODO comment me. *
     */
    protected final void appendToQueryRepresentation(final CharSequence addition) {
        sb.append(addition);
    }

    /**
     * @param addition
     */
    protected final void appendToQueryRepresentation(final char addition) {
        sb.append(addition);
    }


    /**
     * TODO comment me. *
     */
    protected final void insertToQueryRepresentation(final int offset, final CharSequence addition) {
        sb.insert(offset, addition);
    }

    /**
     * TODO comment me. *
     */
    protected final int getQueryRepresentationLength() {
        return sb.length();
    }

    // Query Representation state methods -->

    /**
     * TODO comment me. *
     */
    protected final String getTransformedTerm(final Clause clause) {
        final String transformedTerm = transformedTerms.get(clause);
        return escapeTerm(transformedTerm != null ? transformedTerm : clause.getTerm());

    }

    /**
     * TODO comment me. *
     */
    protected final Map<Clause, String> getTransformedTerms() {
        return transformedTerms;
    }

    /**
     * Setter for property filter.
     *
     * @param filter New value of property filter.
     */
    protected void setFilter(final String filter) {

        LOG.debug("setFilter(\"" + filter + "\")");
        this.filter = filter;
    }

    /**
     * TODO comment me. *
     */
    protected String getAdditionalFilter() {
        return additionalFilter;
    }

    /**
     * returns null when array is null *
     */
    protected final String getSingleParameter(final String paramName) {

        final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
        return parameters.get(paramName) instanceof String[]
                ? ((String[]) parameters.get(paramName))[0]
                : (String) parameters.get(paramName);
    }

    /**
     * Use this always instead of datamodel.getQuery().getQuery()
     * because the command could be running off a different query string.
     *
     * @return
     */
    protected Query getQuery() {

        return query;
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
     * Escapes the term to disarm any reserved words. Override this to do any back-end (index) specific escaping.
     *
     * @param term The term to escape
     * @return The escaped version of term.
     */
    protected String escapeTerm(final String term) {
        return term;
    }

    /**
     * Returns null when no field exists.
     */
    protected final String getFieldFilter(final LeafClause clause) {

        String field = null;
        if (null != clause.getField()) {
            final Map<String, String> fieldFilters = getSearchConfiguration().getFieldFilters();
            if (fieldFilters.containsKey(clause.getField())) {
                field = clause.getField();
            } else {
                final TokenEvaluationEngine engine = getEngine();
                for (String fieldFilter : fieldFilters.keySet()) {
                    try {
                        final TokenPredicate tp = TokenPredicate.valueOf(fieldFilter);
                        // if the field is the token then mask the field and include the term.
                        // XXX why are we checking the known and possible predicates?
                        boolean result = clause.getKnownPredicates().contains(tp);
                        result |= clause.getPossiblePredicates().contains(tp);
                        result &= engine.evaluateTerm(tp, clause.getField());
                        if (result) {
                            field = fieldFilter;
                            break;
                        }
                    } catch (IllegalArgumentException iae) {
                        LOG.trace(TRACE_NOT_TOKEN_PREDICATE + filter);
                    }
                }
            }
        }
        return field;
    }

    /**
     * @param msg
     */
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
     * @return
     */
    protected String getTransformedQuerySesamSyntax() {
        return transformedQuerySesamSyntax;
    }

    /**
     * @return
     */
    protected SesamSyntaxQueryBuilder newSesamSyntaxQueryBuilder() {
        return new SesamSyntaxQueryBuilder();
    }

    protected void updateTransformedQuerySesamSyntax(){

        final SesamSyntaxQueryBuilder builder = newSesamSyntaxQueryBuilder();
        builder.visit(getQuery().getRootClause());
        setTransformedQuerySesamSyntax(builder.getQueryRepresentation());
    }

    protected void setTransformedQuerySesamSyntax(final String sesamSyntax){

        transformedQuerySesamSyntax = sesamSyntax;
    }

    protected final String initialiseTransformedTerms(final Query query){

        // initialise transformed terms
        final Visitor mapInitialisor = new MapInitialisor(transformedTerms);
        mapInitialisor.visit(query.getRootClause());
        return getQueryRepresentation(query);
    }

    protected boolean isEmptyLeaf(final Clause clause) {
        if (clause instanceof LeafClause) {
            final LeafClause leaf = (LeafClause) clause;
            // Changed logic to include: no field and no term. - Geir H. Pettersen - T-Rank
            String transformedTerm = getTransformedTerm(clause);
            transformedTerm = transformedTerm.length() == 0 ? null : transformedTerm;
            return leaf.getField() == null && transformedTerm == null || null != leaf.getField() && null != getFieldFilter(leaf);
        } else if (clause instanceof DoubleOperatorClause) {
            DoubleOperatorClause doc = (DoubleOperatorClause) clause;
            return isEmptyLeaf(doc.getFirstClause()) && isEmptyLeaf(doc.getSecondClause());
        }
        return false;
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

    /**
     * @param transformers
     */
    private void applyQueryTransformers(
            final Query query,
            final TokenEvaluationEngine engine,
            final List<QueryTransformerConfig> transformers) {

        if (transformers != null && transformers.size() > 0) {
            boolean touchedTransformedQuery = false;

            final StringBuilder filterBuilder = new StringBuilder(filter);

            final BaseContext baseQtCxt = new BaseContext() {
                public Site getSite() {
                    return datamodel.getSite().getSite();
                }

                public String getTransformedQuery() {
                    return transformedQuery;
                }

                public Query getQuery() {
                    return query;
                }

                public TokenEvaluationEngine getTokenEvaluationEngine() {
                    return engine;
                }

                public void visitXorClause(Visitor visitor, XorClause clause) {
                    AbstractSearchCommand.this.visitXorClause(visitor, clause);
                }

                public String getFieldFilter(final LeafClause clause) {
                    return AbstractSearchCommand.this.getFieldFilter(clause);
                }
            };

            final QueryTransformerFactory.Context qtfContext = new QueryTransformerFactory.Context() {
                public Site getSite() {
                    return context.getDataModel().getSite().getSite();
                }

                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
            };

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
                        baseQtCxt,
                        context);

                transformer.setContext(qtCxt);

                final String newTransformedQuery = transformer.getTransformedQuery();
                touchedTransformedQuery |= (!transformedQuery.equals(newTransformedQuery));

                if (touchedTransformedQuery) {
                    transformedQuery = newTransformedQuery;
                } else {

                    transformer.visit(query.getRootClause());
                    transformedQuery = getQueryRepresentation(query);
                }

                final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
                final String fp = transformer.getFilter(parameters);
                filterBuilder.append(fp == null ? "" : fp);
                filterBuilder.append(' ');
                final String f = transformer.getFilter();
                filterBuilder.append(f == null ? "" : f);
                filterBuilder.append(' ');

                LOG.debug(transformer.getClass().getSimpleName() + "--> TransformedQuery=" + transformedQuery);
                LOG.debug(transformer.getClass().getSimpleName() + "--> Filter=" + filterBuilder.toString());
            }
            // avoid the trailing space.
            filter = filterBuilder.substring(0, Math.max(0, filterBuilder.length() - 2)).trim();
        } else {
            transformedQuery = getQueryRepresentation(query);
        }

        updateTransformedQuerySesamSyntax();
    }

    /** If the command has been cancelled will throw the appropriate SearchCommandException.
     * Calling this method only makes sense once the call() method has been, 
     *   otherwise it is guaranteed to throw the exception.
     **/
    private void checkForCancellation(){
        if( isCancelled() ){ throw new SearchCommandException("cancelled", new InterruptedException()); }
    }
    
    // Inner classes -------------------------------------------------


    private class MapInitialisor extends AbstractReflectionVisitor {

        private final Map<Clause, String> map;

        public MapInitialisor(final Map<Clause, String> m) {
            map = m;
        }

        protected void visitImpl(final LeafClause clause) {

            if (null == map.get(clause)) {
                if (null != clause.getField()) {
                    if (null == getFieldFilter(clause)) {

                        // Escape any fielded leafs for fields that are not supported by this command.
                        // Performed here in order to make the correct terms visible to the query transformers.
                        map.put(clause, escapeFieldedLeaf(clause));

                    } else {

                        map.put(clause, "");
                    }
                } else {

                    map.put(clause, clause.getTerm());
                }
            }
        }

        protected void visitImpl(final OperationClause clause) {
            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final DoubleOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

    }


    /**
     * Visitor to create the FAST filter string. Handles the site: syntax.
     *
     * @todo add correct handling of NotClause and AndNotClause.
     * This also needs to be added to the query builder visitor above.
     * @todo design for polymorphism and push out fast specifics to appropriate subclass.
     */
    private final class FilterVisitor extends AbstractReflectionVisitor {

        private final StringBuilder filterBuilder = new StringBuilder();

        String getFilter() {
            return filterBuilder.toString().trim();
        }

        protected void visitImpl(final LeafClause clause) {

            final String field = getFieldFilter(clause);
            if (null != field) {
                appendFilter(clause.getTerm(), field);
            }
        }

        protected void visitImpl(final PhraseClause clause) {

            final String field = getFieldFilter(clause);
            if (null != field) {
                appendFilter(clause.getTerm(), field);
            }
        }

        protected void visitImpl(final DoubleOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final XorClause clause) {
            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final NotClause clause) {
            // ignore fields inside NOTs for now
        }

        protected void visitImpl(final AndNotClause clause) {
            // ignore fields inside NOTs for now
        }

        private void appendFilter(String term, final String field) {

            final Map<String, String> fieldFilters = getSearchConfiguration().getFieldFilters();
            if ("site".equals(field)) {
                // XXX fast specific stuff. push down to fast command.
                // site fields do not accept quotes
                term = term.replaceAll("\"", "");
            }
            final String fieldAs = fieldFilters.get(field);


            // XXX fast specific stuff. push down to fast command.
            boolean fastCommandConfig = false;
            try{
                fastCommandConfig = Class.forName("no.sesat.search.mode.config.FastCommandConfig")
                    .isAssignableFrom(getSearchConfiguration().getClass());

            }catch(ClassNotFoundException cnfe){
                LOG.trace("Configuration is not FastCommandConfig");
            }

            if (fastCommandConfig) {

                boolean adv = false;
                try{
                    final SearchConfiguration fsc = getSearchConfiguration();
                    final Method m = fsc.getClass().getMethod("getFiltertype", new Class[]{});
                    adv = "adv".equals(m.invoke(fsc, new Object[]{}));

                }catch(NoSuchMethodException nsme){
                    LOG.fatal("Expected method in FastCommandConfig. " + nsme.getMessage());
                }catch(IllegalAccessException iae){
                    LOG.error(iae.getMessage());
                }catch(InvocationTargetException ite){
                    LOG.error(ite.getMessage());
                }
                if (adv){
                    filterBuilder.append(" AND " + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));
                }else{
                    filterBuilder.append(" +" + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));
                }
            } else {
                filterBuilder.append(" +" + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));
            }
        }


    }

    /**
     * Query builder for creating a query syntax similar to sesam's own.
     */
    protected final class SesamSyntaxQueryBuilder extends AbstractReflectionVisitor {

        private final StringBuilder sb = new StringBuilder();
        private boolean insideOr = false;

        /**
         * Returns the generated query.
         *
         * @return The query.
         */
        String getQueryRepresentation() {
            return sb.toString().trim();
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final LeafClause clause) {

            final String field = clause.getField();

            // ignore terms that are fielded and terms that have been initialised but since nulled
            if (null == field && null != transformedTerms.get(clause)) {
                sb.append(transformedTerms.get(clause));
            }
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final AndClause clause) {

            clause.getFirstClause().accept(this);
            sb.append(" +");
            clause.getSecondClause().accept(this);
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final OrClause clause) {

            boolean wasInside = insideOr;
            if (!insideOr) {
                sb.append('(');
            }
            insideOr = true;
            clause.getFirstClause().accept(this);
            sb.append(' ');
            clause.getSecondClause().accept(this);
            insideOr = wasInside;
            if (!insideOr) {
                sb.append(')');
            }
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final DefaultOperatorClause clause) {

            clause.getFirstClause().accept(this);
            sb.append(' ');
            clause.getSecondClause().accept(this);
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final NotClause clause) {

            final String childsTerm = transformedTerms.get(clause.getFirstClause());
            if (childsTerm != null && childsTerm.length() > 0) {
                sb.append('-');
                clause.getFirstClause().accept(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final AndNotClause clause) {

            final String childsTerm = transformedTerms.get(clause.getFirstClause());
            if (childsTerm != null && childsTerm.length() > 0) {
                sb.append('-');
                clause.getFirstClause().accept(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        protected void visitImpl(final XorClause clause) {

            switch (clause.getHint()) {
                case FULLNAME_ON_LEFT:
                    clause.getSecondClause().accept(this);
                    break;
                default:
                    AbstractSearchCommand.this.visitXorClause(this, clause);
            }
        }
    }

    /**
     * see createQuery(string). *
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


}
