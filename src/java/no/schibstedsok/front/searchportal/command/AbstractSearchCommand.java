package no.schibstedsok.front.searchportal.command;

import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.query.QueryTransformer;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.result.ResultHandler;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>.
 * @version <tt>$Revision$</tt>
 */
public abstract class AbstractSearchCommand implements SearchCommand {
    private static Log log = LogFactory.getLog(AbstractSearchCommand.class);

    private RunningQuery query;
    private String filter;
    private String transformedQuery;
    protected SearchConfiguration configuration;
    private Map parameters;

    static String[] stopWordExpressions = { "cataloguePrefix",
                                            "picturePrefix",
                                            "newsPrefix",
                                            "tvPrefix",
                                            "weatherPrefix"};


    /**
     * @param query         The query to act on.
     * @param configuration The search configuration associated with this
     *                      command.
     * @param parameters    Command parameters.
     */
    public AbstractSearchCommand(final RunningQuery query,
                                 final SearchConfiguration configuration,
                                 final Map parameters) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: AbstractSearchCommand()");
        }
        this.query = query;
        this.configuration = configuration;
        this.parameters = parameters;
    }

    /**
     * Returns the query with which this command is associated.
     *
     * @return The Query.
     */
    public final RunningQuery getQuery() {
        if(log.isDebugEnabled()){
            log.debug("ENTR: getQuery()");
        }
        return query;
    }

    public abstract SearchResult execute();

    /**
     * Called by thread executor
     * @return
     */
    public Object call() {
        if(log.isDebugEnabled()){
            log.debug("ENTR: call()");
        }
        String queryToUse;

        if (configuration.getUseParameterAsQuery() != null) {
            queryToUse = getSingleParameter(configuration.getUseParameterAsQuery());
        } else {
            queryToUse = getQuery().getQueryString();
        }
        transformedQuery = queryToUse;
        applyQueryTransformers(configuration.getQueryTransformers());
        StopWatch watch = null;

        if (log.isDebugEnabled()) {
            watch = new StopWatch();
            watch.start();
        }
        SearchResult result = null;

        //TODO: Hide this in QueryRule.execute(some parameters)
        boolean executeQuery = false;

        if(queryToUse.length() > 0){
            executeQuery = true;
        }
        if(parameters.get("contentsource") != null){
            if(log.isDebugEnabled()){
                log.debug("call: Got contentsource, executeQuery=true");
            }
            executeQuery = true;
        }

        if(filter != null){
            executeQuery = true;
        }

        if(log.isDebugEnabled()){
            log.debug("call(): ExecuteQuery?" + executeQuery);
        }
        result = executeQuery ? execute() : new BasicSearchResult(this);

        if (log.isDebugEnabled()) {
            watch.stop();

            log.debug("Hits is " + configuration.getName() + ":" + result.getHitCount());
            log.debug("Search " + configuration.getName() + " took " + watch);
        }

        for (Iterator handlerIterator = configuration.getResultHandlers().iterator(); handlerIterator.hasNext();) {
            ResultHandler resultHandler = (ResultHandler) handlerIterator.next();
            resultHandler.handleResult(result, parameters);
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
        if (configuration.isPagingEnabled()) {
            return i + query.getOffset();
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
    private void applyQueryTransformers(List transformers) {
        if (transformers != null) {
            for (Iterator iterator = transformers.iterator(); iterator.hasNext();) {
                QueryTransformer transformer = (QueryTransformer) iterator.next();

                transformedQuery = transformer.getTransformedQuery(transformedQuery);

                if(filter == null){
                    filter = transformer.getFilter(transformedQuery);
                } else if(transformer.getFilter(transformedQuery) != null){
                    filter += transformer.getFilter(transformedQuery) + " ";
                }

                if(log.isDebugEnabled()){
                    log.debug("applyQueryTransformers: TransformedQuery=" + transformedQuery);
                    log.debug("applyQueryTransformers: Filter=" + filter);
                }
            }
        }

    }

    public SearchConfiguration getSearchConfiguration() {
        return configuration;
    }

    protected Map getParameters() {
        return parameters;
    }

    private String getSingleParameter(String paramName) {
        return ((String[])parameters.get(paramName))[0];
    }

    public String toString() {
        return configuration.getName() + " " + query.getQueryString();
    }

    public String getFilter() {
        return filter;
    }
}
