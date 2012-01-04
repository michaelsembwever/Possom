/* Copyright (2012) Schibsted ASA
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
 * AbstractRunningQuery.java
 *
 * Created on 16 February 2006, 19:49
 *
 */

package no.sesat.search.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.InfrastructureException;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.MapDataObject;
import no.sesat.search.datamodel.generic.MapDataObjectSupport;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.navigation.NavigationDataObject;
import no.sesat.search.datamodel.query.QueryDataObject;
import no.sesat.search.mode.SearchCommandFactory;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.mode.executor.SearchCommandExecutor;
import no.sesat.search.mode.executor.SearchCommandExecutorFactory;
import no.sesat.search.query.Query;
import no.sesat.search.query.QueryStringContext;
import no.sesat.search.query.parser.AbstractQueryParserContext;
import no.sesat.search.query.parser.QueryParser;
import no.sesat.search.query.parser.QueryParserImpl;
import no.sesat.search.query.token.DeadTokenEvaluationEngineImpl;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenEvaluationEngineImpl;
import no.sesat.search.result.NavigationItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.run.handler.NavigationRunHandlerConfig;
import no.sesat.search.run.handler.RunHandler;
import no.sesat.search.run.handler.RunHandlerConfig;
import no.sesat.search.run.handler.RunHandlerFactory;
import no.sesat.search.run.transform.RunTransformer;
import no.sesat.search.run.transform.RunTransformerConfig;
import no.sesat.search.run.transform.RunTransformerFactory;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.PropertiesLoader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** Basic implementation around
 *      - the execution of a set of SearchCommands,
 *      - the pre transformation of the set of commands before they are executed, and
 *      - the post handling on the set of commands after executed, eg federation.
 *
 * @version $Id$
 */
public abstract class AbstractRunningQuery implements RunningQuery {

   // Constants -----------------------------------------------------

    private static final int TIMEOUT = Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)
            ? 10000
            : Integer.MAX_VALUE;

    private static final Logger LOG = Logger.getLogger(AbstractRunningQuery.class);
    private static final Logger PRODUCT_LOG = Logger.getLogger("no.sesat.Product");
    private static final String ERR_MODE_TIMEOUT = "Timeout running search commands.";
    private static final String ERR_RUN_QUERY = "Failure to run query";
    private static final String ERR_EXECUTION_ERROR = "Failure in a search command.";
    private static final String INFO_COMMAND_COUNT = "Commands to invoke ";

    // TODO generic parameter key to be put into ParameterDataObject
    protected static final String PARAM_COMMANDS = "commands";
    // TODO generic parameter key to be put into ParameterDataObject
    private static final String PARAM_WAITFOR = "waitFor";

    // TODO generic parameter key to be put into ParameterDataObject
    public static final String PARAM_LAYOUT = "layout";

    // Attributes ----------------------------------------------------

    protected final Context context;
    private final DataModel datamodel;
    private final Map<String,Integer> hits = new HashMap<String,Integer>();
    private final StringBuilder noHitsOutput = new StringBuilder();

    /** The TokenEvaluationEngine that will be used. */
    protected final TokenEvaluationEngine engine;

    /** have all search commands been cancelled.
     * implementation details allowing web subclasses to send correct error to client. **/
    private boolean allCancelled = false;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of AbstractRunningQuery */
    protected AbstractRunningQuery(
            final Context cxt,
            final String query) throws SiteKeyedFactoryInstantiationException {

        context = cxt;
        datamodel = cxt.getDataModel();


        final String queryStr = truncate(trimDuplicateSpaces(query));

        final SiteContext siteCxt = new SiteContext(){
            @Override
            public Site getSite() {
                return datamodel.getSite().getSite();
            }
        };

        final TokenEvaluationEngine.Context tokenEvalFactoryCxt =
                ContextWrapper.wrap(
                TokenEvaluationEngine.Context.class,
                context,
                new QueryStringContext() {
                    @Override
                    public String getQueryString() {
                        return queryStr;
                    }
                },
                new BaseContext() {
                    public String getUniqueId() {
                        return datamodel.getParameters().getUniqueId();
                    }
                },
                siteCxt);

        if(cxt.getSearchMode().isEvaluation()){
            engine = new TokenEvaluationEngineImpl(tokenEvalFactoryCxt);

        }else{
            // use a dead token evaluation engine. false and stale evaluation so it is not cached.
            engine = new DeadTokenEvaluationEngineImpl(tokenEvalFactoryCxt);
        }

        // queryStr parser
        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
            @Override
            public TokenEvaluationEngine getTokenEvaluationEngine() {
                return engine;
            }
        });

        final DataModelFactory factory
                = DataModelFactory.instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, cxt, siteCxt));

        final QueryDataObject queryDO = factory.instantiate(
                QueryDataObject.class,
                datamodel,
                new DataObject.Property("string", queryStr),
                new DataObject.Property("query", parser.getQuery()));

        final MapDataObject<NavigationItem> navigations
                = new MapDataObjectSupport<NavigationItem>(Collections.<String, NavigationItem>emptyMap());

        final NavigationDataObject navDO = factory.instantiate(
                NavigationDataObject.class,
                datamodel,
                new DataObject.Property("configuration", context.getSearchTab().getNavigationConfiguration()),
                new DataObject.Property("navigation", navigations),
                new DataObject.Property("navigations", navigations)); // TODO bug that both single and mapped needed

        datamodel.setQuery(queryDO);
        datamodel.setNavigation(navDO);

    }

    // Public --------------------------------------------------------

    /**
     * Thread run. Guts of the logic behind this class.
     *
     * @throws InterruptedException
     */
    @Override
    public void run() throws InterruptedException {

        LOG.debug("run()");

        final Map<String,StringDataObject> parameters = datamodel.getParameters().getValues();

        try {

            final DataModelFactory dataModelFactory =  DataModelFactory
                    .instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, context, new SiteContext(){
                        @Override
                        public Site getSite(){
                            return datamodel.getSite().getSite();
                        }
                    }));

            // DataModel's ControlLevel will be RUNNING_QUERY_CONSTRUCTION
            //  Increment it onwards to SEARCH_COMMAND_CONSTRUCTION.
            dataModelFactory.assignControlLevel(datamodel, ControlLevel.SEARCH_COMMAND_CONSTRUCTION);

            final Collection<SearchCommand> commands = buildCommands();

            LOG.info(INFO_COMMAND_COUNT + commands.size());

            // mark state that we're about to execute the sub threads
            setAllCancelled(commands.size() > 0);
            boolean hitsToShow = false;

            // DataModel's ControlLevel will be SEARCH_COMMAND_CONSTRUCTION
            //  Increment it onwards to SEARCH_COMMAND_CONSTRUCTION.
            dataModelFactory.assignControlLevel(datamodel, ControlLevel.SEARCH_COMMAND_EXECUTION);

            final Map<Future<ResultList<ResultItem>>,SearchCommand> results = executeSearchCommands(commands);

            // DataModel's ControlLevel will be SEARCH_COMMAND_CONSTRUCTION
            //  Increment it onwards to RUNNING_QUERY_RESULT_HANDLING.
            dataModelFactory.assignControlLevel(datamodel, ControlLevel.RUNNING_QUERY_HANDLING);

            if( !isAllCancelled() ){

                for (Future<ResultList<ResultItem>> task : results.keySet()) {

                    if (task.isDone() && !task.isCancelled()) {

                        try{
                            if (null != task.get()) {
                                hitsToShow |= postProcessTask(task, results);
                            }
                        }catch(ExecutionException ee){
                            LOG.error(ERR_EXECUTION_ERROR, ee);
                        }
                    }
                }

                performHandlers();

                if (!hitsToShow) {
                    handleNoHits(noHitsOutput, parameters);
                }
            }

        } catch (Exception e) {
            LOG.error(ERR_RUN_QUERY, e);
            throw new InfrastructureException(e);

        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /** Intentionally overridable. Would be nice if run-transform-spi could have influence on the result here.
     *
     * @return collection of SearchConfigurations applicable to this running query.
     */
    protected Collection<SearchConfiguration> applicableSearchConfigurations(){

        final Collection<SearchConfiguration> applicableSearchConfigurations = new ArrayList<SearchConfiguration>();

        final String[] explicitCommands = null != datamodel.getParameters().getValue(PARAM_COMMANDS)
                ? datamodel.getParameters().getValue(PARAM_COMMANDS).getString().split(",")
                : new String[0];

        for (SearchConfiguration conf : context.getSearchMode().getSearchConfigurations()) {

            // everything on by default if explicitCommands is undefined
            boolean applicable = (0 == explicitCommands.length);

            // check for specified list of commands to run in url
            for(String explicitCommand : explicitCommands){
                applicable |= explicitCommand.equalsIgnoreCase(conf.getId());
            }

            // check for alwaysRun. this implementation doesn't handle any conditional stuff when isAlwaysRun is false
            applicable &= conf.isAlwaysRun();

            // add search configuration if applicable
            if(applicable){
                applicableSearchConfigurations.add(conf);
            }
        }

        return applicableSearchConfigurations;
    }

    protected boolean addCommand(final SearchCommand.Context searchCmdCxt){

        return true;
    }

    protected Collection<SearchCommand> buildCommands(){

        final Collection<SearchCommand> commands = new ArrayList<SearchCommand>();
        final SearchCommandFactory.Context scfContext = new SearchCommandFactory.Context() {
            @Override
            public Site getSite() {
                return context.getDataModel().getSite().getSite();
            }
            @Override
            public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                return context.newBytecodeLoader(site, name, jar);
            }
        };

        final SearchCommandFactory searchCommandFactory = new SearchCommandFactory(scfContext);

        for (SearchConfiguration searchConfiguration : performTransformers(applicableSearchConfigurations())) {

            final SearchConfiguration config = searchConfiguration;
            final String confName = config.getId();

            try{

                hits.put(confName, Integer.valueOf(0));

                final SearchCommand.Context searchCmdCxt = ContextWrapper.wrap(
                        SearchCommand.Context.class,
                        context,
                        new BaseContext() {
                            public SearchConfiguration getSearchConfiguration() {
                                return config;
                            }
                            public RunningQuery getRunningQuery() {
                                return AbstractRunningQuery.this;
                            }
                            public Query getQuery() {
                                return datamodel.getQuery().getQuery();
                            }
                            public TokenEvaluationEngine getTokenEvaluationEngine() {
                                return engine;
                            }
                        }
                );

                if (addCommand(searchCmdCxt)) {
                    commands.add(searchCommandFactory.getController(searchCmdCxt));
                }
            }catch(RuntimeException re){
                LOG.error("Failed to add command " + confName, re);
            }
        }
        return commands;
    }

    /**
     * Remote duplicate spaces. Leading and trailing spaces will
     * be preserved
     * @param query that may conaint duplicate spaces
     * @return string with duplicate spaces removed
     */
    protected static String trimDuplicateSpaces(final String query){

        LOG.trace("trimDuplicateSpaces(" + query + ")");
        return null == query ? null : query.replaceAll("\\s+", " ").trim();
    }

    @SuppressWarnings(value = "unchecked")
    protected Map<Future<ResultList<ResultItem>>, SearchCommand> executeSearchCommands(
            final Collection<SearchCommand> commands)
            throws InterruptedException, TimeoutException, ExecutionException {

        Map<Future<ResultList<ResultItem>>, SearchCommand> results = Collections.EMPTY_MAP;
        try {
            final SearchCommandExecutor executor
                    = SearchCommandExecutorFactory.getController(context.getSearchMode().getExecutor());

            try {
                results = executor.invokeAll(commands);
            } finally {
                final Map<Future<ResultList<ResultItem>>, SearchCommand> waitFor;
                if (null != datamodel.getParameters().getValue(PARAM_WAITFOR)) {
                    waitFor = new HashMap<Future<ResultList<ResultItem>>, SearchCommand>();

                    final String[] waitForArr
                            = datamodel.getParameters().getValue(PARAM_WAITFOR).getString().split(",");

                    for (String waitForStr : waitForArr) {
                        // using generics on the next line crashes javac
                        for (Entry entry : results.entrySet()) {

                            final String entryName
                                    = ((SearchCommand) entry.getValue()).getSearchConfiguration().getId();

                            if (waitForStr.equalsIgnoreCase(entryName)) {

                                waitFor.put(
                                        (Future<ResultList<ResultItem>>) entry.getKey(),
                                        (SearchCommand) entry.getValue());
                                break;
                            }
                        }
                    }
                } else if (null != datamodel.getParameters().getValue(PARAM_COMMANDS)) {
                    // wait on everything explicitly asked for
                    waitFor = results;
                } else {
                    // do not wait on asynchronous commands
                    waitFor = new HashMap<Future<ResultList<ResultItem>>, SearchCommand>();
                    // using generics on the next line crashes javac
                    for (Entry entry : results.entrySet()) {
                        if (!((SearchCommand) entry.getValue()).getSearchConfiguration().isAsynchronous()) {

                            waitFor.put(
                                    (Future<ResultList<ResultItem>>) entry.getKey(),
                                    (SearchCommand) entry.getValue());
                        }
                    }
                }
                executor.waitForAll(waitFor, TIMEOUT);
            }
        } catch (TimeoutException te) {
            LOG.error(ERR_MODE_TIMEOUT);
        }

        for (SearchCommand command : commands) {
            allCancelled &= (null != datamodel.getParameters().getValue(PARAM_COMMANDS)
                    || !command.getSearchConfiguration().isAsynchronous());

            allCancelled &= command.isCancelled();
        }
        return results;
    }

    protected boolean postProcessTask(
            final Future<ResultList<ResultItem>> task,
            final Map<Future<ResultList<ResultItem>>,SearchCommand> results)
            throws ExecutionException, InterruptedException {

        final ResultList<ResultItem> searchResult = task.get();
        // Information we need about and for the enrichment
        final SearchCommand command = results.get(task);
        final SearchConfiguration config = command.getSearchConfiguration();

        final String name = config.getId();

        // update hit status
        hits.put(name, searchResult.getHitCount());

        if( 0 >= searchResult.getHitCount() && command.isPaginated() ){
            noHitsOutput.append("<command id=\"" + config.getId()
                    + "\" name=\""  + config.getStatisticalName()
                    + "\" type=\"" + config.getClass().getSimpleName()
                    + "\"/>");
        }

        return 0 < searchResult.getHitCount();
    }

    protected boolean isAllCancelled(){
        return allCancelled;
    }

    protected void setAllCancelled(final boolean allCancelled){
        this.allCancelled = allCancelled;
    }

    protected Map<String,Integer> getHits(){
        return Collections.unmodifiableMap(hits);
    }

    /** Truncates string to an acceptable length. **/
    protected String truncate(final String query){

        // generic.sesam defines a default value of 256
        final int length = Integer.parseInt(
                context.getDataModel().getSite().getSiteConfiguration().getProperty("sesat.query.characterLimit"));

        return length < query.length()
                ? query.substring(0, length)
                : query;
    }

    protected Collection<SearchConfiguration> performTransformers(
            final Collection<SearchConfiguration> applicableSearchConfigurations) {

        final RunTransformer.Context transformerContext = new RunTransformer.Context() {
                    @Override
                    public Collection<SearchConfiguration>getApplicableSearchConfigurations() {
                        return applicableSearchConfigurations;
                    }
                    @Override
                    public DataModel getDataModel() {
                        return datamodel;
                    }
                    @Override
                    public DocumentLoader newDocumentLoader(
                            final SiteContext siteContext,
                            final String resource,
                            final DocumentBuilder builder) {
                        return context.newDocumentLoader(siteContext, resource, builder);
                    }
                    @Override
                    public PropertiesLoader newPropertiesLoader(
                            final SiteContext siteContext,
                            final String resource,
                            final Properties properties) {
                        return context.newPropertiesLoader(siteContext, resource, properties);
                    }
                    @Override
                    public BytecodeLoader newBytecodeLoader(
                            final SiteContext siteContext,
                            final String className,
                            final String jarFileName) {
                        return context.newBytecodeLoader(siteContext, className, jarFileName);
                    }
                    @Override
                    public Site getSite() {
                        return datamodel.getSite().getSite();
                    }
        };

        final List<RunTransformerConfig> rtcList = context.getSearchMode().getRunTransformers();

        for (final RunTransformerConfig rtc : rtcList) {
            final RunTransformer rt = RunTransformerFactory.getController(transformerContext, rtc);
            rt.transform(transformerContext);
        }

        return applicableSearchConfigurations;
    }

    protected void performHandlers(){

        final RunHandler.Context handlerContext = ContextWrapper.wrap(
                RunHandler.Context.class,
                new SiteContext(){
                    @Override
                    public Site getSite() {
                        return datamodel.getSite().getSite();
                    }
                },
                context);

        final List<RunHandlerConfig> rhcList
                = new ArrayList<RunHandlerConfig>(context.getSearchMode().getRunHandlers());

        /* Adding NavigationRunHandler to all search modes. TODO move into modes.xml */
        rhcList.add(new NavigationRunHandlerConfig());

        for (final RunHandlerConfig rhc : rhcList) {
            final RunHandler rh = RunHandlerFactory.getController(handlerContext, rhc);

            LOG.debug("executing " + rh);
            rh.handleRunningQuery(handlerContext);
        }
    }

    private void handleNoHits(final StringBuilder noHitsOutput, final Map<String,StringDataObject> parameters)
            throws SiteKeyedFactoryInstantiationException, InterruptedException{

        // there were no hits for any of the search tabs!
        noHitsOutput.append("<absolute/>");

        if( noHitsOutput.length() >0 && datamodel.getQuery().getString().length() >0
                && !"NOCOUNT".equals(parameters.get("IGNORE"))){

            final String layout = null != parameters.get(PARAM_LAYOUT)
                    ? parameters.get(PARAM_LAYOUT).getString()
                    : null;

            noHitsOutput.insert(0, "<no-hits mode=\"" + context.getSearchTab().getKey()
                    + (null != layout ? "\" layout=\"" + layout : "") + "\">"
                    + "<query>" + datamodel.getQuery().getXmlEscaped() + "</query>");
            noHitsOutput.append("</no-hits>");
            PRODUCT_LOG.info(noHitsOutput.toString());
        }

        // maybe we can modify the query to broaden the search
        // replace all DefaultClause with an OrClause
        //  [simply done with wrapping the query string inside ()'s ]
        final String queryStr = datamodel.getQuery().getString();

        if (!queryStr.startsWith("(") && !queryStr.endsWith(")")
                && datamodel.getQuery().getQuery().getTermCount() > 1
                && context.getSearchMode().isAutoBroadening()) {

            // DataModel's ControlLevel will be RUNNING_QUERY_CONSTRUCTION
            //  Increment it onwards to SEARCH_COMMAND_CONSTRUCTION.
            final DataModelFactory dataModelFactory =  DataModelFactory
                    .instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, context, new SiteContext(){
                        @Override
                        public Site getSite(){
                            return datamodel.getSite().getSite();
                        }
                    }));
            dataModelFactory.assignControlLevel(datamodel, ControlLevel.RUNNING_QUERY_CONSTRUCTION);

            // create and run a new RunningQueryImpl
            new RunningQueryImpl(context, '(' + queryStr + ')').run();

            // TODO put in some sort of feedback to user that query has been changed.
        }

    }

    // Private -------------------------------------------------------

}
