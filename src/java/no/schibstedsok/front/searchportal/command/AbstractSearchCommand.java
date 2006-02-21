// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.command;

import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.query.Query;
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
public abstract class AbstractSearchCommand implements SearchCommand {
    private static final Logger LOG = Logger.getLogger(AbstractSearchCommand.class);

    protected final Context context;
    private String filter;
    private String transformedQuery;
    private Map parameters;

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

    public abstract SearchResult execute();

    /**
     * Called by thread executor
     * @return
     */
    public Object call() {
        MDC.put(Site.NAME_KEY, context.getSite().getName());

        if (getSearchConfiguration().getStatisticsName() != null) {
            LOG.info("STATISTICS: " + getSearchConfiguration().getStatisticsName());
        }

        LOG.trace("call()");
        String queryToUse;

        if (getSearchConfiguration().getUseParameterAsQuery() != null) {
            queryToUse = getSingleParameter(getSearchConfiguration().getUseParameterAsQuery());
        } else {
            queryToUse = context.getRunningQuery().getStrippedQueryString();
        }
        transformedQuery = queryToUse;
        applyQueryTransformers(getSearchConfiguration().getQueryTransformers());
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

        for (Iterator handlerIterator = getSearchConfiguration().getResultHandlers().iterator(); handlerIterator.hasNext();) {
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
    }

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

    /**
     * Returns the query as it is after the query transformers have been
     * applied to it.
     *
     * @return The transformed query.
     */
    public String getTransformedQuery() {
        return transformedQuery;
    }

    /**
     *
     * @param transformers
     */
    private void applyQueryTransformers(final List transformers) {
        if (transformers != null) {
            for (Iterator iterator = transformers.iterator(); iterator.hasNext();) {
                final QueryTransformer transformer = (QueryTransformer) iterator.next();
                final QueryTransformer.Context qtCxt = new QueryTransformer.Context() {
                    public String getQueryString() {
                        return transformedQuery;
                    }

                    public Site getSite() {
                        return context.getSite();
                    }

                };


                transformedQuery = transformer.getTransformedQuery(qtCxt);


                if (filter == null) {
                    filter = transformer.getFilter(qtCxt, parameters);
                } else if (transformer.getFilter(qtCxt, parameters) != null) {
                    filter += transformer.getFilter(qtCxt, parameters) + " ";
                }

                if (filter == null) {
                    filter = transformer.getFilter(qtCxt);
                } else if (transformer.getFilter(qtCxt) != null) {
                    filter += transformer.getFilter(qtCxt) + " ";
                }
		
                if (LOG.isDebugEnabled()) {
                    LOG.debug("applyQueryTransformers: TransformedQuery=" + transformedQuery);
                    LOG.debug("applyQueryTransformers: Filter=" + filter);
                }
            }
        }

    }

    public SearchConfiguration getSearchConfiguration() {
        return context.getSearchConfiguration();
    }

    protected Map getParameters() {
        return parameters;
    }

    private String getSingleParameter(final String paramName) {
        return ((String[]) parameters.get(paramName))[0];
    }

    public String toString() {
        return getSearchConfiguration().getName() + " " + context.getRunningQuery().getQueryString();
    }

    public String getFilter() {
        return filter;
    }
}
