// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.command;


import java.util.LinkedHashMap;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
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
import no.schibstedsok.searchportal.query.QueryStringContext;
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
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.view.config.SearchTab;
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

    private static final String FIELD_TRANSFORMED_QUERY = "transformedQuery";

    private static final Logger LOG = Logger.getLogger(AbstractSearchCommand.class);
    private static final Logger STATISTICS_LOG = Logger.getLogger("no.schibstedsok.Statistics");

    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";
    private static final String ERR_TRANSFORMED_QUERY_USED
            = "Cannot use transformedTerms Map once deprecated getTransformedQuery as been used";
    private static final String ERR_HANDLING_CANCELLATION
            = "Cancellation (and now handling of) occurred to ";
    private static final String TRACE_NOT_TOKEN_PREDICATE = "Not a TokenPredicate ";

    static{

        // when the root logger is set to DEBUG do not limit connection times
        if(Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)){
            System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
            System.setProperty("sun.net.client.defaultReadTimeout", "3000");
        }
    }

   // Attributes ----------------------------------------------------

    /** TODO comment me. **/
    protected final Context context;
    private String filter = "";
    private final String additionalFilter;
    private final Map<Clause,String> transformedTerms = new LinkedHashMap<Clause,String>();
    private String transformedQuery;
    private final Map<String,Object> parameters;
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

        this.parameters = parameters;

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

    /** TODO comment me. **/
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



    /** TODO comment me. **/
    public String toString() {
        return getSearchConfiguration().getName() + " " + context.getRunningQuery().getQueryString();
    }

    /** TODO comment me. **/
    public String getFilter() {
        return filter;
    }


   // SearchCommand overrides ---------------------------------------------------

    /** TODO comment me. **/
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
        final String statName = getSearchConfiguration().getStatisticalName();
        if (statName != null && statName.length()>0) {
            Thread.currentThread().setName(thread + " [" + getSearchConfiguration().getStatisticalName() + "]");
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

    /** TODO comment me. **/
    public void handleCancellation(){

        if(!completed){
            LOG.error(ERR_HANDLING_CANCELLATION
                    + getSearchConfiguration().getName()
                    + " [" + getClass().getSimpleName() + "]");

            performResultHandling(new BasicSearchResult(this));
        }
    }

    // AbstractReflectionVisitor overrides ----------------------------------------------

    private final StringBuilder sb = new StringBuilder();

    /** TODO comment me. **/
    protected void visitImpl(final LeafClause clause) {

        final String field = clause.getField();
        if (field == null) {
            sb.append(transformedTerms.get(clause));
        }
    }
    /** TODO comment me. **/
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        sb.append(" AND ");
        clause.getSecondClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final OrClause clause) {
        clause.getFirstClause().accept(this);
        sb.append(" OR ");
        clause.getSecondClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        sb.append(' ');
        clause.getSecondClause().accept(this);
    }
    /** TODO comment me. **/
    protected void visitImpl(final NotClause clause) {
        final String childsTerm = (String) transformedTerms.get(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            sb.append("NOT ");
            clause.getFirstClause().accept(this);
        }
    }
    /** TODO comment me. **/
    protected void visitImpl(final AndNotClause clause) {
        final String childsTerm = (String) transformedTerms.get(clause.getFirstClause());
        if (childsTerm != null && childsTerm.length() > 0) {
            sb.append("ANDNOT ");
            clause.getFirstClause().accept(this);
        }
    }
    /** TODO comment me. **/
    protected void visitImpl(final XorClause clause) {
        // [TODO] we need to determine which branch in the query-tree we want to use.
        //  Both branches to a XorClause should never be used.
        clause.getFirstClause().accept(this);
        // clause.getSecondClause().accept(this);
    }

   // Protected -----------------------------------------------------


    /** TODO comment me. **/
    protected final String performQueryTransformation(){

        // use the query or something search-command specific
        final boolean useParameterAsQuery = getSearchConfiguration().getQueryParameter() != null
                && getSearchConfiguration().getQueryParameter().length() >0;
        // so what query string is it then
        final String queryToUse = useParameterAsQuery
                ? getSingleParameter(getSearchConfiguration().getQueryParameter())
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

    /** TODO comment me. **/
    protected final SearchResult performExecution(final String queryToUse){

        final StopWatch watch = new StopWatch();
        watch.start();
        Integer hitCount = null;

        try{

            //TODO: Hide this in QueryRule.execute(some parameters)
            boolean executeQuery = queryToUse.length() > 0;
            executeQuery |= null != parameters.get("contentsource");
            executeQuery |= null != parameters.get("newscountry");
            executeQuery |= null != parameters.get("c") && parameters.get("c").equals("wt");
            executeQuery |= null != parameters.get("c") && parameters.get("c").equals("n");

            executeQuery |= null != filter && filter.length() > 0;
            LOG.debug("executeQuery==" + executeQuery
                    + " ; queryToUse:" + queryToUse
                    + "; filter:" + filter
                    + "; tabKey:" + parameters.get("c") + ';');

            final SearchResult result = executeQuery ? execute() : new BasicSearchResult(this);
            hitCount = result.getHitCount();

            LOG.info("Hits is " + getSearchConfiguration().getName() + ':' + hitCount);

            return result;

        }finally{

            watch.stop();
            LOG.info("Search " + getSearchConfiguration().getName() + " took " + watch);

            STATISTICS_LOG.info(
                "<search-command name=\"" + getSearchConfiguration().getStatisticalName() + "\">"
                    + "<query>" + context.getQuery().getQueryString() + "</query>"
                    + "<search-name>" + getClass().getSimpleName() + "</search-name>"
                    + (hitCount != null ? "<hits>" + hitCount + "</hits>" : "<failure/>")
                    + "<time>" + watch + "</time>"
                + "</search-command>");
        }
    }

    /** TODO comment me. **/
    protected final void performResultHandling(final SearchResult result){

        // add some general properties first from the command
        //  if we've somehow blanked out the query altogether then revert to the user's origianl query string
        result.addField(FIELD_TRANSFORMED_QUERY,
                getTransformedQuery().length()>0 ? getTransformedQuery() : context.getQuery().getQueryString());

        // process listed result handlers
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
        if (getSearchConfiguration().isPaging()) {
            final Object v = parameters.get("offset");
            return i + Integer.parseInt(v instanceof String[] && ((String[])v).length ==1
                    ? ((String[]) v)[0]
                    : (String) v);
        } else {
            return i;
        }
    }

    /** TODO comment me. **/
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
                final String[] val = (String[]) parameters.get(paramName);
                if (val.length > 0 && val[0].length() > 0) {
                    return val[0];
                }
            }else if(parameters.get(paramName) instanceof String){
                return (String)parameters.get(paramName);
            }
        }
        return "";
    }

    /** TODO comment me. **/
    protected synchronized String getQueryRepresentation(final Query query) {

        final Clause root = query.getRootClause();
        sb.setLength(0);
        visit(root);
        return sb.toString().trim();
    }

    /** TODO comment me. **/
    protected final void appendToQueryRepresentation(final CharSequence addition) {
        sb.append(addition);
    }

    /** TODO comment me. **/
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

    /** TODO comment me. **/
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

            final BaseContext baseQtCxt = new BaseContext(){
                public String getTransformedQuery() {
                    return transformedQuery;
                }
                public Query getQuery() {
                    return query;
                }
                public TokenEvaluationEngine getTokenEvaluationEngine(){
                    return context.getRunningQuery().getTokenEvaluationEngine();
                }
            };

            for (QueryTransformer transformer : transformers) {

                final boolean ttq = touchedTransformedQuery;

                final QueryTransformer.Context qtCxt = ContextWrapper.wrap(
                        QueryTransformer.Context.class,
                        new BaseContext(){
                            public Map<Clause,String> getTransformedTerms() {
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

                LOG.debug(transformer.getClass().getSimpleName() + "--> TransformedQuery=" + transformedQuery);
                LOG.debug(transformer.getClass().getSimpleName() + "--> Filter=" + filter);
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


        final TokenEvaluationEngineImpl.Context tokenEvalFactoryCxt = ContextWrapper.wrap(
                TokenEvaluationEngineImpl.Context.class,
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return queryString;
                        }
                    }
        );

        // This will among other things perform the initial fast search
        // for textual analysis.
        final TokenEvaluationEngine tokenEvaluationEngine = new TokenEvaluationEngineImpl(tokenEvalFactoryCxt);

        // queryStr parser
        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
            public TokenEvaluationEngine getTokenEvaluationEngine() {
                return tokenEvaluationEngine;
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

        private void visitImpl(final NotClause clause){
            // ignore fields inside NOTs for now
        }

        private void visitImpl(final AndNotClause clause){
            // ignore fields inside NOTs for now
        }

        private void appendFilter(String term, final String field) {

            final Map<String,String> fieldFilters = getSearchConfiguration().getFieldFilters();
            if("site".equals(field)){
                // site fields do not accept quotes
                term = term.replaceAll("\"","");
            }
            final String fieldAs = fieldFilters.get(field);
            filterBuilder.append(" +" + (fieldAs.length() >0 ?  fieldAs + ':' + term : term));
        }

        /** Returns null when no field exists. **/
        private String getFieldFilter(final LeafClause clause){

            String field = null;
            if(null != clause.getField()){
                final Map<String,String> fieldFilters = getSearchConfiguration().getFieldFilters();
                if(fieldFilters.containsKey(clause.getField())){
                    field = clause.getField();
                }else{
                    final TokenEvaluationEngine evalEngine = context.getRunningQuery().getTokenEvaluationEngine();
                    for(String fieldFilter : fieldFilters.keySet()){
                        try{
                            final TokenPredicate tp = TokenPredicate.valueOf(fieldFilter);
                            // if the field is the token then mask the field and include the term.
                            boolean result = clause.getKnownPredicates().contains(tp);
                            result |= clause.getPossiblePredicates().contains(tp);
                            result &= evalEngine.evaluateTerm(tp, clause.getField());
                            if(result){
                                field = fieldFilter;
                                break;
                            }
                        }catch(IllegalArgumentException iae){
                            LOG.trace(TRACE_NOT_TOKEN_PREDICATE + filter);
                        }
                    }
                }
            }
            return field;
        }
    }


}
