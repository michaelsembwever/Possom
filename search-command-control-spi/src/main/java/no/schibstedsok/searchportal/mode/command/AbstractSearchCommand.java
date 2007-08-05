// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;


import java.io.Serializable;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.mode.config.FastCommandConfig;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.parser.QueryParser;
import no.schibstedsok.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.searchportal.query.parser.TokenMgrError;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.query.transform.QueryTransformer;
import no.schibstedsok.searchportal.query.transform.QueryTransformerConfig;
import no.schibstedsok.searchportal.query.transform.QueryTransformerFactory;
import no.schibstedsok.searchportal.result.BasicResultList;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.result.handler.DataModelResultHandler;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.handler.ResultHandlerConfig;
import no.schibstedsok.searchportal.result.handler.ResultHandlerFactory;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.BytecodeLoader;
import no.schibstedsok.searchportal.view.config.SearchTab;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>.
 * @version <tt>$Id$</tt>
 */
abstract class AbstractSearchCommand extends AbstractReflectionVisitor implements SearchCommand, Serializable {

    // Constants -----------------------------------------------------

    private static final DataModelResultHandler DATAMODEL_HANDLER = new DataModelResultHandler();

    private static final Logger LOG = Logger.getLogger(AbstractSearchCommand.class);
    protected static final Logger DUMP = Logger.getLogger("no.schibstedsok.searchportal.Dump");

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
    protected final DataModel datamodel;
    /**
     *
     */
    protected volatile boolean completed = false;
    private volatile Thread thread = null;

    // Static --------------------------------------------------------

    /**
     */
    protected static final ResultList<? extends ResultItem> getSearchResult(
            final String id,
            final DataModel datamodel) throws InterruptedException {

        synchronized (datamodel.getSearches()) {
            while (null == datamodel.getSearch(id)) {
                // next line releases the monitor so it is possible to call this method from different threads
                datamodel.getSearches().wait();
            }
        }
        return datamodel.getSearch(id).getResults();
    }

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
        thread = Thread.currentThread();

        final String t = thread.getName();
        final String statName = getSearchConfiguration().getStatisticalName();

        if (statName != null && statName.length() > 0) {
            Thread.currentThread().setName(t + " [" + getSearchConfiguration().getStatisticalName() + ']');
        } else {
            Thread.currentThread().setName(t + " [" + getClass().getSimpleName() + ']');
        }

        try {

            LOG.trace("call()");

            performQueryTransformation();
            final ResultList<? extends ResultItem> result = performExecution();
            performResultHandling(result);


            completed = true;
            thread = null;
            return result;

        } catch (RuntimeException rte) {
            LOG.error(ERROR_RUNTIME, rte);
            return new BasicResultList<ResultItem>();

        } finally {
            // restore thread name
            Thread.currentThread().setName(t);
        }

    }

    /**
     * TODO comment me. *
     */
    public synchronized boolean handleCancellation() {

        if (!completed) {
            LOG.error(ERR_HANDLING_CANCELLATION
                    + getSearchConfiguration().getName()
                    + " [" + getClass().getSimpleName() + "]");

            if (null != thread) {
                thread.interrupt();
                thread = null;
            }
            performResultHandling(new BasicResultList());
        }
        return !completed;
    }

    // AbstractReflectionVisitor overrides ----------------------------------------------

    private final StringBuilder sb = new StringBuilder();

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
        final String childsTerm = (String) transformedTerms.get(clause.getFirstClause());
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

        final String query = getTransformedQuery().trim();
        Integer hitCount = null;

        try {

            // we will be executing the command IF there's a valid query or filter,
            // or if the configuration specifies that we should run anyway.
            boolean executeQuery = "*".equals(datamodel.getQuery().getString());
            executeQuery |= query.length() > 0 || getSearchConfiguration().isRunBlank();
            executeQuery |= null != filter && 0 < filter.length();
            executeQuery |= null != additionalFilter && 0 < additionalFilter.length();

            LOG.info("executeQuery==" + executeQuery + " ; query:" + query + " ; filter:" + filter);

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

            ResultHandlerFactory.getController(resultHandlerConfig).handleResult(resultHandlerContext, datamodel);
        }

        // The DataModel result handler is a hardcoded feature of the architecture
        DATAMODEL_HANDLER.handleResult(resultHandlerContext, datamodel);
        // also ping everybody that might be waiting on these results: "dinner's served!"
        synchronized (datamodel.getSearches()) {
            datamodel.getSearches().notifyAll();
        }
    }

    /**
     * Returns the offset in the result set. If paging is enabled for the
     * current search configuration the offset to the current page will be
     * added to the parameter.
     *
     * @param i the current offset.
     * @return i plus the offset of the current page.
     */
    protected int getCurrentOffset(final int i) {

        final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
        final Object v = parameters.get("offset");

        if (null != v && getSearchConfiguration().isPaging()) {
            return i + Integer.parseInt(v instanceof String[] && ((String[]) v).length == 1
                    ? ((String[]) v)[0]
                    : (String) v);
        } else {
            return i;
        }
    }

    /**
     * TODO comment me. *
     *
     * @return
     */
    protected Map<String, Object> getParameters() {

        final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
        return parameters;
    }

    /**
     * Returns parameter value. In case the parameter is multi-valued only the
     * first value is returned. If the parameter does not exist or is empty
     * the empty string is returned.
     *
     * @param paramName
     */
    protected String getParameter(final String paramName) {

        final Map<String, Object> parameters = datamodel.getJunkYard().getValues();
        if (parameters.containsKey(paramName)) {
            if (parameters.get(paramName) instanceof String[]) {
                final String[] val = (String[]) parameters.get(paramName);
                if (val.length > 0 && val[0].length() > 0) {
                    return val[0];
                }
            } else if (parameters.get(paramName) instanceof String) {
                return (String) parameters.get(paramName);
            }
        }
        return "";
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
     * Returns null when no field exists. *
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
        final StringBuffer logger = (StringBuffer) parameters.get("no.schibstedsok.Statistics");
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
                LOG.debug(transformer.getClass().getSimpleName() + "--> Filter=" + filter);
            }
            // avoid the trailing space.
            filter = filterBuilder.substring(0, Math.max(0, filterBuilder.length() - 2)).trim();
        } else {
            transformedQuery = getQueryRepresentation(query);
        }

        updateTransformedQuerySesamSyntax();
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

            if (getSearchConfiguration() instanceof FastCommandConfig) {
                final FastCommandConfig fsc = (FastCommandConfig) getSearchConfiguration();
                if ("adv".equals(fsc.getFiltertype()))
                    filterBuilder.append(" AND " + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));
                else
                    filterBuilder.append(" +" + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));
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
            return sb.toString();
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
