// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.command;


import java.util.HashMap;
import java.util.LinkedHashMap;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.query.Visitor;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.front.searchportal.query.parser.QueryParser;
import no.schibstedsok.front.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.front.searchportal.query.parser.TokenMgrError;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.query.transform.QueryTransformer;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.view.config.SearchTab;
import org.apache.commons.lang.time.StopWatch;


import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>.
 *
 * @version <tt>$Id$</tt>
 */
public abstract class AbstractSearchCommand extends AbstractReflectionVisitor implements SearchCommand {

   // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractSearchCommand.class);
    private static final Logger STATISTICS_LOG = Logger.getLogger("no.schibstedsok.Statistics");

    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";
    private static final String ERR_TRANSFORMED_QUERY_USED
            = "Cannot use transformedTerms Map once deprecated getTransformedQuery as been used";
    private static final String ERR_HANDLING_CANCELLATION
            = "Cancellation (and now handling of) occurred to ";

    static{

        // when the root logger is set to DEBUG do not limit connection times
        if(Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)){
            System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
            System.setProperty("sun.net.client.defaultReadTimeout", "3000");
        }
    }

   // Attributes ----------------------------------------------------

    protected final Context context;
    private String filter = "";
    private final String additionalFilter;
    private final Map<Clause,String> transformedTerms = new LinkedHashMap<Clause,String>();
    private String transformedQuery;
    private volatile Map<String,Object> parameters;
    private volatile boolean completed = false;


   // Constructors --------------------------------------------------

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public AbstractSearchCommand(final SearchCommand.Context cxt,
                                 final Map<String,Object> parameters) {

        LOG.trace("AbstractSearchCommand()");
        context = cxt;

        // Hack to keep vg site search working. Dependent on old query
        // parameters. Remove when vg has been reimplented a proper site search.
        Map<String, Object> m = new HashMap<String, Object>();
        
        m.putAll(parameters);
        
        if (m.containsKey("nav_newspaperNames")) {
            m.put("nav_newspaperNames", "newssourcenavigator");
        }

        if (m.containsKey("ywpopnavn")) {
            m.put("newssource", m.get("ywpopnavn"));
            m.remove("ywpopnavn");
        }
        
        this.parameters = m;
        // XXX should be null so we know neither applyQueryTransformers or performQueryTranformation has been called
        transformedQuery = context.getQuery().getQueryString();
        final Clause root = context.getQuery().getRootClause();

        // initialise transformed terms
        final Visitor mapInitialisor = new MapInitialisor(transformedTerms);
        mapInitialisor.visit(root);

        // create additional filters
        final FilterVisitor  additionalFilterVisitor = new FilterVisitor();
        additionalFilterVisitor.visit(context.getQuery().getRootClause());
        additionalFilter = additionalFilterVisitor.getFilter();
    }

   // Public --------------------------------------------------------

    public abstract SearchResult execute();

    /**
     * Returns the query as it is after the query transformers have been
     * applied to it.
     *
     * @return The transformed query.
     */
    public String getTransformedQuery() {
        return transformedQuery;
    }



    public String toString() {
        return getSearchConfiguration().getName() + " " + context.getRunningQuery().getQueryString();
    }

    public String getFilter() {
        return filter;
    }


   // SearchCommand overrides ---------------------------------------------------

    public SearchConfiguration getSearchConfiguration() {
        return context.getSearchConfiguration();
    }

    /**
     * Returns the query with which this command is associated.
     *
     * @return The Query.
     */
    public final RunningQuery getRunningQuery() {
        LOG.trace("getQuery()");
        return context.getRunningQuery();
    }

    /**
     * Called by thread executor
     * @return
     */
    public SearchResult call() {

        MDC.put(Site.NAME_KEY, context.getSite().getName());

        final String thread = Thread.currentThread().getName();
        final String statName = getSearchConfiguration().getStatisticsName();
        if (statName != null && statName.length()>0) {
            Thread.currentThread().setName(thread + " [" + getSearchConfiguration().getStatisticsName() + "]");
        }  else  {
            Thread.currentThread().setName(thread + " [" + getClass().getSimpleName() + "]");
        }
        try  {

            LOG.trace("call()");

            final String queryToUse = performQueryTransformation();
            final SearchResult result = performExecution(queryToUse);
            performResultHandling(result);


            completed = true;
            return result;

        }  finally  {
            // restore thread name
            Thread.currentThread().setName(thread);
        }
    }

    public void handleCancellation(){

        if(!completed){
            LOG.error(ERR_HANDLING_CANCELLATION
                    + getSearchConfiguration().getName()
                    + " [" + getClass().getSimpleName() + "]");

            performResultHandling(new BasicSearchResult(this));
        }
    }

    // AbstractReflectionVisitor overrides ----------------------------------------------

    private final StringBuffer sb = new StringBuffer();

    protected void visitImpl(final LeafClause clause) {
        if (clause.getField() == null) {
            sb.append(transformedTerms.get(clause));
        }
    }
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        sb.append(" AND ");
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final OrClause clause) {
        clause.getFirstClause().accept(this);
        sb.append(" OR ");
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        sb.append(' ');
        clause.getSecondClause().accept(this);
    }
    protected void visitImpl(final NotClause clause) {
        final String childsTerm = (String) transformedTerms.get(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            sb.append("NOT ");
            clause.getFirstClause().accept(this);
        }
    }
    protected void visitImpl(final AndNotClause clause) {
        final String childsTerm = (String) transformedTerms.get(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            sb.append("ANDNOT ");
            clause.getFirstClause().accept(this);
        }
    }
    protected void visitImpl(final XorClause clause) {
        // [TODO] we need to determine which branch in the query-tree we want to use.
        //  Both branches to a XorClause should never be used.
        clause.getFirstClause().accept(this);
        // clause.getSecondClause().accept(this);
    }

   // Protected -----------------------------------------------------


    protected final String performQueryTransformation(){

        // use the query or something search-command specific
        final boolean useParameterAsQuery = getSearchConfiguration().getUseParameterAsQuery() != null
                && getSearchConfiguration().getUseParameterAsQuery().length() >0;
        // so what query string is it then
        final String queryToUse = useParameterAsQuery
                ? getSingleParameter(getSearchConfiguration().getUseParameterAsQuery())
                : context.getQuery().getQueryString();

        if (useParameterAsQuery) {
            // OOBS. It's not the query we are looking for but a string held
            // in a different parameter.
            transformedQuery = queryToUse;
            final Query query = createQuery(queryToUse);
            transformedTerms.clear();
            // re-initialise map with new query's terms.
            final Visitor mapInitialisor = new MapInitialisor(transformedTerms);
            mapInitialisor.visit(query.getRootClause());
            applyQueryTransformers(query, getSearchConfiguration().getQueryTransformers());
        }  else  {
            applyQueryTransformers(context.getQuery(), getSearchConfiguration().getQueryTransformers());
        }
        return queryToUse;
    }

    protected final SearchResult performExecution(final String queryToUse){

        StopWatch watch = null;
        if (LOG.isDebugEnabled()) {
            watch = new StopWatch();
            watch.start();
        }
        try{

            //TODO: Hide this in QueryRule.execute(some parameters)
            boolean executeQuery = queryToUse.length() > 0;
            if (parameters.get("contentsource") != null || parameters.get("newscountry") != null) {
                LOG.debug("call: Got contentsource, executeQuery=true");
                executeQuery = true;
            }

            executeQuery |= filter != null && filter.length() > 0;
            LOG.debug("executeQuery==" + executeQuery + " ; queryToUse:" + queryToUse + "; filter:" + filter + ";");

            final SearchResult result = executeQuery ? execute() : new BasicSearchResult(this);

            STATISTICS_LOG.info(
                    "<search-command name=\"" + getSearchConfiguration().getStatisticsName() + "\">"
                        + "<query>" + context.getQuery().getQueryString() + "</query>"
                        + "<search-name>"
                        + getClass().getSimpleName()
                        + "</search-name>"
                        + "<hits>" + result.getHitCount() + "</hits>"
                    + "</search-command>");
            LOG.debug("Hits is " + getSearchConfiguration().getName() + ":" + result.getHitCount());

            return result;

        }finally{
            if (LOG.isDebugEnabled()) {
                watch.stop();
                LOG.debug("Search " + getSearchConfiguration().getName() + " took " + watch);
            }
        }
    }

    protected final void performResultHandling(final SearchResult result){

        for (ResultHandler resultHandler : getSearchConfiguration().getResultHandlers()) {

            final ResultHandler.Context resultHandlerContext = ContextWrapper.wrap(
                    ResultHandler.Context.class,
                    new BaseContext(){// <editor-fold defaultstate="collapsed" desc=" ResultHandler.Context ">
                        public SearchResult getSearchResult() {
                            return result;
                        }
                        public SearchTab getSearchTab(){
                            return context.getRunningQuery().getSearchTab();
                        }
                        /** @deprecated implementations should be using the QueryContext instead! */
                        public String getQueryString() {
                            return context.getRunningQuery().getQueryString();
                        }
                        public void addSource(final Modifier modifier) {
                            context.getRunningQuery().addSource(modifier);
                        }
                    },// </editor-fold>
                    context
            );
            resultHandler.handleResult(resultHandlerContext, parameters);
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
        if (getSearchConfiguration().isPagingEnabled()) {
            final Object v = parameters.get("offset");
            return i + Integer.parseInt(v instanceof String[] && ((String[])v).length ==1
                    ? ((String[]) v)[0]
                    : (String) v);
        } else {
            return i;
        }
    }

    protected Map getParameters() {
        return parameters;
    }

    /**
     * Returns parameter value. In case the parameter is multi-valued only the
     * first value is returned. If the parameter does not exist or is empty
     * the empty string is returned.
     */
    protected String getParameter(final String paramName) {
        if (parameters.containsKey(paramName)) {
            if(parameters.get(paramName) instanceof String[]){
                final String val[] = (String[]) parameters.get(paramName);
                if (val.length > 0 && val[0].length() > 0) {
                    return val[0];
                }
            }else if(parameters.get(paramName) instanceof String){
                return (String)parameters.get(paramName);
            }
        }
        return "";
    }

    protected synchronized String getQueryRepresentation(final Query query) {

        final Clause root = query.getRootClause();
        sb.setLength(0);
        visit(root);
        return sb.toString().trim();
    }

    protected final void appendToQueryRepresentation(final CharSequence addition) {
        sb.append(addition);
    }

    protected final String getTransformedTerm(final Clause clause) {
        return (String) transformedTerms.get(clause);
    }

    /**
     * Setter for property filter.
     * @param filter New value of property filter.
     */
    protected void setFilter(final String filter) {

        LOG.debug("setFilter(\"" + filter + "\")");
        this.filter = filter;
    }

    protected String getAdditionalFilter() {
        return additionalFilter;
    }

    // Private -------------------------------------------------------

    /**
     *
     * @param transformers
     */
    private void applyQueryTransformers(final Query query, final List<QueryTransformer> transformers) {
        if (transformers != null && transformers.size() > 0) {
            boolean touchedTransformedQuery = false;

            final StringBuilder filterBuilder = new StringBuilder(filter);

            for (QueryTransformer transformer : transformers) {

                final boolean ttq = touchedTransformedQuery;

                final QueryTransformer.Context qtCxt = ContextWrapper.wrap(
                        QueryTransformer.Context.class,
                        new BaseContext(){
                            public String getTransformedQuery() {
                                return transformedQuery;
                            }
                            public Map<Clause,String> getTransformedTerms() {
                                if (ttq) {
                                    throw new IllegalStateException(ERR_TRANSFORMED_QUERY_USED);
                                }
                                return transformedTerms;
                            }
                            public Query getQuery() {
                                return query;
                            }
                            public TokenEvaluatorFactory getTokenEvaluatorFactory(){
                                return context.getRunningQuery().getTokenEvaluatorFactory();
                            }
                        },
                        context);
                transformer.setContext(qtCxt);

                final String newTransformedQuery = transformer.getTransformedQuery();
                touchedTransformedQuery |= (!transformedQuery.equals(newTransformedQuery));

                if (touchedTransformedQuery) {
                    transformedQuery = newTransformedQuery;
                }  else  {

                    transformer.visit(query.getRootClause());
                    transformedQuery = getQueryRepresentation(query);
                }

                final String fp = transformer.getFilter(parameters);
                filterBuilder.append(fp == null ? "" : fp);
                filterBuilder.append(' ');
                final String f = transformer.getFilter();
                filterBuilder.append(f == null ? "" : f);
                filterBuilder.append(' ');
		
                if (LOG.isDebugEnabled()) {
                    LOG.debug("applyQueryTransformers: TransformedQuery=" + transformedQuery);
                    LOG.debug("applyQueryTransformers: Filter=" + filter);
                }
            }
            // avoid the trailing space.
            filter = filterBuilder.substring(0, Math.max(0, filterBuilder.length() - 2)).trim();
        } else {
            transformedQuery = getQueryRepresentation(query);
        }
    }

    /** returns null when array is null **/
    protected final String getSingleParameter(final String paramName) {
        return parameters.get(paramName) instanceof String[]
                ? ((String[]) parameters.get(paramName))[0]
                : (String) parameters.get(paramName);
    }

    private Query createQuery(final String queryString) {


        final TokenEvaluatorFactoryImpl.Context tokenEvalFactoryCxt = ContextWrapper.wrap(
                TokenEvaluatorFactoryImpl.Context.class,
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return queryString;
                        }
                    }
        );

        // This will among other things perform the initial fast search
        // for textual analysis.
        final TokenEvaluatorFactory tokenEvaluatorFactory = new TokenEvaluatorFactoryImpl(tokenEvalFactoryCxt);

        // queryStr parser
        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
            public TokenEvaluatorFactory getTokenEvaluatorFactory() {
                return tokenEvaluatorFactory;
            }
        });

        try  {
            return parser.getQuery();

        } catch (TokenMgrError ex)  {
            // Errors (as opposed to exceptions) are fatal.
            LOG.fatal(ERR_PARSING, ex);
        }
        return null;
    }

   // Inner classes -------------------------------------------------


    private static class MapInitialisor extends AbstractReflectionVisitor {

        private final Map map;

        public MapInitialisor(final Map m) {
            map = m;
        }

        protected void visitImpl(final LeafClause clause) {
            map.put(clause, clause.getTerm());
        }
        protected void visitImpl(final OperationClause clause) {
            clause.getFirstClause().accept(this);
        }
        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        protected void visitImpl(final OrClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
    }


    /**
     *
     * Visitor to create the FAST filter string. Handles the site: syntax.
     *
     * @todo add correct handling of NotClause and AndNotClause. This also needs
     * to be added to the query builder visitor above.
     *
     */
    private final class FilterVisitor extends AbstractReflectionVisitor {

        private final StringBuilder filterBuilder = new StringBuilder();

        String getFilter(){
            return filterBuilder.toString().trim();
        }

        protected void visitImpl(final LeafClause clause) {

            final Map<String,String> fieldFilters = getSearchConfiguration().getFieldFilters();
            if (fieldFilters.containsKey(clause.getField())) {
                appendFilter(clause);
            }
        }

        protected void visitImpl(final PhraseClause clause) {

            final Map<String,String> fieldFilters = getSearchConfiguration().getFieldFilters();
            if (fieldFilters.containsKey(clause.getField())) {
                appendFilter(clause);
            }
        }

        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final OrClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final XorClause clause) {
            clause.getFirstClause().accept(this);
        }

        private final void appendFilter(final LeafClause clause) {

            final Map<String,String> fieldFilters = getSearchConfiguration().getFieldFilters();
            if("site".equals(clause.getField())){
                // site fields do not accept quotes
                final String term = clause.getTerm().replaceAll("\"","");
                filterBuilder.append(" +" + fieldFilters.get(clause.getField()) + ":" + term);
            }else{
                filterBuilder.append(" +" + fieldFilters.get(clause.getField()) + ":" + clause.getTerm());
            }
        }
    }


}
