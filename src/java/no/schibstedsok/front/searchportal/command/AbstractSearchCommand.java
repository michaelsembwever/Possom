// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.command;

import com.thoughtworks.xstream.XStream;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
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
import org.apache.commons.lang.time.StopWatch;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>.
 *
 * @version <tt>$Revision$</tt>
 */
public abstract class AbstractSearchCommand extends AbstractReflectionVisitor implements SearchCommand {

   // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractSearchCommand.class);
    private static final Logger STATISTICS_LOG = Logger.getLogger("no.schibstedsok.Statistics");

    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";
    private static final String ERR_TRANSFORMED_QUERY_USED
            = "Cannot use transformedTerms Map once deprecated getTransformedQuery as been used";

   // Attributes ----------------------------------------------------

    protected final Context context;
    private String filter = "";
    private final Map<Clause,String> transformedTerms = new LinkedHashMap<Clause,String>();
    private String transformedQuery;
    private Map parameters;


   // Constructors --------------------------------------------------

    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public AbstractSearchCommand(final SearchCommand.Context cxt,
                                 final Map parameters) {

        LOG.trace("AbstractSearchCommand()");
        context = cxt;
        this.parameters = parameters;
        transformedQuery = context.getQuery().getQueryString();
        final Clause root = context.getQuery().getRootClause();
        final Visitor mapInitialisor = new MapInitialisor(transformedTerms);
        mapInitialisor.visit(root);
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
    public Object call() {
        MDC.put(Site.NAME_KEY, context.getSite().getName());

        final String thread = Thread.currentThread().getName();
        if (getSearchConfiguration().getStatisticsName() != null) {
            //LOG.info("STATISTICS: " + getSearchConfiguration().getStatisticsName());
            Thread.currentThread().setName(thread + " [" + getSearchConfiguration().getStatisticsName() + "]");
        }  else  {
            Thread.currentThread().setName(
                    thread+" ["+getClass().getSimpleName()+"]");
        }
        try  {


            LOG.trace("call()");
            final String queryToUse = getSearchConfiguration().getUseParameterAsQuery() != null
                    ? getSingleParameter(getSearchConfiguration().getUseParameterAsQuery())
                    : context.getQuery().getQueryString();



            if (getSearchConfiguration().getUseParameterAsQuery() != null) {
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

            StopWatch watch = null;

            if (LOG.isDebugEnabled()) {
                watch = new StopWatch();
                watch.start();
            }
            //SearchResult result = null;

            //TODO: Hide this in QueryRule.execute(some parameters)
            boolean executeQuery = false;

            if (queryToUse.length() > 0) {
                executeQuery = true;
            }
            if (parameters.get("contentsource") != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("call: Got contentsource, executeQuery=true");
                }
                executeQuery = true;
            }

            if (filter != null) {
                executeQuery = true;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("call(): ExecuteQuery?" + executeQuery);
            }
            final SearchResult result = executeQuery ? execute() : new BasicSearchResult(this);

            if (LOG.isDebugEnabled()) {
                watch.stop();

                LOG.debug("Hits is " + getSearchConfiguration().getName() + ":" + result.getHitCount());
                LOG.debug("Search " + getSearchConfiguration().getName() + " took " + watch);
            }
            STATISTICS_LOG.info(
                    "<search-command name=\"" + getSearchConfiguration().getStatisticsName() + "\">"
                        + "<query>" + context.getQuery().getQueryString() + "</query>"
                        + "<search-name>"
                        + getClass().getSimpleName()
                        + "</search-name>"
                        + "<hits>" + result.getHitCount() + "</hits>"
                    + "</search-command>");

            for (final Iterator handlerIterator = getSearchConfiguration().getResultHandlers().iterator(); handlerIterator.hasNext();) {
                final ResultHandler resultHandler = (ResultHandler) handlerIterator.next();
                final ResultHandler.Context resultHandlerContext = new ResultHandler.Context() {
                    // <editor-fold defaultstate="collapsed" desc=" ResultHandler.Context ">
                    public SearchResult getSearchResult() {
                        return result;
                    }

                    public Site getSite() {
                        return context.getSite();
                    }

                    public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                        return context.newPropertiesLoader(resource, properties);
                    }

                    public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                        return context.newXStreamLoader(resource, xstream);
                    }

                    public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                        return context.newDocumentLoader(resource, builder);
                    }

                    public Query getQuery() {
                        return context.getQuery();
                    }
                    /** @deprecated implementations should be using the QueryContext instead! */
                    public String getQueryString() {
                        return context.getRunningQuery().getQueryString();
                    }

                    public void addSource(final Modifier modifier) {
                        context.getRunningQuery().addSource(modifier);
                    }
                    // </editor-fold>
                };
                resultHandler.handleResult(resultHandlerContext, parameters);
            }
            return result;

        }  finally  {
            // restore thread name
            Thread.currentThread().setName(thread);
        }
    }

   // AbstractReflectionVisitor overrides ----------------------------------------------

    private final StringBuffer sb = new StringBuffer();

    protected void visitImpl(final LeafClause clause) {
        final String fullTerm =
                (clause.getField() == null ? "" : clause.getField() + ": ")
                + clause.getTerm();

        sb.append(transformedTerms.get(clause));
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

    /**
     * Returns the offset in the result set. If paging is enabled for the
     * current search configuration the offset to the current page will be
     * added to the parameter.
     *
     * @param i the current offset.
     * @return i plus the offset of the current page.
     */
    protected final int getCurrentOffset(final int i) {
        if (getSearchConfiguration().isPagingEnabled()) {
            return i + context.getRunningQuery().getOffset();
        } else {
            return i;
        }
    }

    protected Map getParameters() {
        return parameters;
    }

    protected final synchronized String getQueryRepresentation(final Query query) {

        final Clause root = query.getRootClause();
        sb.setLength(0);
        visit(root);
        return sb.toString();
    }

    protected final void appendToQueryRepresentation(final CharSequence addition) {
        sb.append(addition);
    }

    protected final String getTransformedTerm(final Clause clause) {
        return (String) transformedTerms.get(clause);
    }

    // Private -------------------------------------------------------

    /**
     *
     * @param transformers
     */
    private void applyQueryTransformers(final Query query, final List transformers) {
        if (transformers != null) {


            boolean touchedTransformedQuery = false;

            // initialise map with default values


            final StringBuilder filterBuilder = new StringBuilder();

            for (final Iterator iterator = transformers.iterator(); iterator.hasNext();) {

                final QueryTransformer transformer = (QueryTransformer) iterator.next();
                final boolean ttq = touchedTransformedQuery;

                final QueryTransformer.Context qtCxt = new QueryTransformer.Context() {

                    public String getTransformedQuery() {
                        return transformedQuery;
                    }

                    public Map<Clause,String> getTransformedTerms() {
                        if (ttq) {
                            throw new IllegalStateException(ERR_TRANSFORMED_QUERY_USED);
                        }
                        return transformedTerms;
                    }

                    public Site getSite() {
                        return context.getSite();
                    }

                    public Query getQuery() {
                        return query;
                    }

                };
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

            filter = filterBuilder.substring(0, Math.max(0, filterBuilder.length() - 2)); // avoid the trailing space.
        }
    }

    private String getSingleParameter(final String paramName) {
        return ((String[]) parameters.get(paramName))[0];
    }

    private Query createQuery(final String queryString) {


        final TokenEvaluatorFactoryImpl.Context tokenEvalFactoryCxt = ContextWrapper.wrap(
                TokenEvaluatorFactoryImpl.Context.class,
                new BaseContext[]{
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return queryString;
                        }
                    }
        });

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
}
