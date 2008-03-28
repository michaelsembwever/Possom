/*
 * Copyright (2005-2007) Schibsted Søk AS
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
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
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
import no.sesat.search.query.analyser.AnalysisRule;
import no.sesat.search.query.analyser.AnalysisRuleFactory;
import no.sesat.search.query.QueryStringContext;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenEvaluationEngineImpl;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.SearchCommandFactory;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.mode.executor.SearchCommandExecutor;
import no.sesat.search.mode.executor.SearchCommandExecutorFactory;
import no.sesat.search.query.parser.AbstractQueryParserContext;
import no.sesat.search.query.Query;
import no.sesat.search.query.parser.QueryParser;
import no.sesat.search.query.parser.QueryParserImpl;
import no.sesat.search.query.token.TokenEvaluationEngineContext;
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
import no.sesat.search.view.config.SearchTab.EnrichmentHint;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Central controlling class around the individual search commands executed in any query search.
 *
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class RunningQueryImpl extends AbstractRunningQuery implements RunningQuery {

   // Constants -----------------------------------------------------

    private static final int TIMEOUT = Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO)
            ? 10000
            : Integer.MAX_VALUE;

    // TODO generic parameter key to be put into ParameterDataObject
    public static final String PARAM_LAYOUT = "layout";
    // TODO generic parameter key to be put into ParameterDataObject
    private static final String PARAM_COMMANDS = "commands";
    // TODO generic parameter key to be put into ParameterDataObject
    private static final String PARAM_WAITFOR = "waitFor";

    private static final Logger LOG = Logger.getLogger(RunningQueryImpl.class);
    private static final Logger ANALYSIS_LOG = Logger.getLogger("no.sesat.search.analyzer.Analysis");
    private static final Logger PRODUCT_LOG = Logger.getLogger("no.sesat.Product");

    private static final String ERR_RUN_QUERY = "Failure to run query";
    private static final String ERR_EXECUTION_ERROR = "Failure in a search command.";
    private static final String ERR_MODE_TIMEOUT = "Timeout running all search commands.";
    private static final String INFO_COMMAND_COUNT = "Commands to invoke ";

    // Attributes ----------------------------------------------------

    private final AnalysisRuleFactory rules;

    /** have all search commands been cancelled.
     * implementation details allowing web subclasses to send correct error to client. **/
    protected boolean allCancelled = false;
    /** */
    protected final DataModel datamodel;
    /** */
    protected final TokenEvaluationEngine engine;
    private final Map<String,Integer> hits = new HashMap<String,Integer>();
    private final Map<String,Integer> scores = new HashMap<String,Integer>();
    private final Map<String,Integer> scoresByRule = new HashMap<String,Integer>();

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Create a new RunningQuery instance.
     *
     * @param cxt
     * @param query
     * @throws no.sesat.search.site.SiteKeyedFactoryInstantiationException
     */
    public RunningQueryImpl(
            final Context cxt,
            final String query) throws SiteKeyedFactoryInstantiationException {

        super(cxt);
        this.datamodel = cxt.getDataModel();

        LOG.trace("RunningQuery(cxt," + query + ')');

        final String queryStr = trimDuplicateSpaces(query);

        final SiteContext siteCxt = new SiteContext(){
            public Site getSite() {
                return datamodel.getSite().getSite();
            }
        };

        final TokenEvaluationEngine.Context tokenEvalFactoryCxt =
                ContextWrapper.wrap(
                    TokenEvaluationEngine.Context.class,
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return queryStr;
                        }
                    },
                    siteCxt);

        // This will among other things perform the initial fast search
        // for textual analysis.
        engine = new TokenEvaluationEngineImpl(tokenEvalFactoryCxt);

        // queryStr parser
        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
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
                new DataObject.Property("navigation",navigations),
                new DataObject.Property("navigations", navigations)); // FIXME bug that both single and mapped needed

        datamodel.setQuery(queryDO);
        datamodel.setNavigation(navDO);

        rules = AnalysisRuleFactory.instanceOf(ContextWrapper.wrap(AnalysisRuleFactory.Context.class, context, siteCxt));

    }

    // Public --------------------------------------------------------

    /**
     * Thread run. Guts of the logic behind this class.
     * XXX long method. Divide & Conquer.
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {

        LOG.debug("run()");
        final StringBuilder analysisReport
                = new StringBuilder(" <analyse><query>" + datamodel.getQuery().getXmlEscaped() + "</query>\n");

        final Map<String,StringDataObject> parameters = datamodel.getParameters().getValues();

        try {

            final DataModelFactory dataModelFactory =  DataModelFactory
                    .instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, context, new SiteContext(){
                        public Site getSite(){
                            return datamodel.getSite().getSite();
                        }
                    }));

            // DataModel's ControlLevel will be RUNNING_QUERY_CONSTRUCTION
            //  Increment it onwards to SEARCH_COMMAND_CONSTRUCTION.
            dataModelFactory.assignControlLevel(datamodel, ControlLevel.SEARCH_COMMAND_CONSTRUCTION);

            final Collection<SearchCommand> commands = new ArrayList<SearchCommand>();

            final SearchCommandFactory.Context scfContext = new SearchCommandFactory.Context() {
                public Site getSite() {
                    return context.getDataModel().getSite().getSite();
                }
                public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                    return context.newBytecodeLoader(site, name, jar);
                }
            };

            final SearchCommandFactory searchCommandFactory = new SearchCommandFactory(scfContext);

            for (SearchConfiguration searchConfiguration : applicableSearchConfigurations()) {

                final SearchConfiguration config = searchConfiguration;
                final String confName = config.getName();

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
                                    return RunningQueryImpl.this;
                                }
                                public Query getQuery() {
                                    return datamodel.getQuery().getQuery();
                                }
                                public TokenEvaluationEngine getTokenEvaluationEngine() {
                                    return engine;
                                }
                            }
                    );

                    final EnrichmentHint eHint = context.getSearchTab().getEnrichmentByCommand(confName);
                    if (eHint != null && !datamodel.getQuery().getQuery().isBlank()) {

                        // search command marked as an enrichment
                        if(useEnrichment(eHint, config, searchCmdCxt, analysisReport)){
                            commands.add(searchCommandFactory.getController(searchCmdCxt));
                        }

                    }else{

                        // normal search command
                        commands.add(searchCommandFactory.getController(searchCmdCxt));
                    }
                }catch(RuntimeException re){
                    LOG.error("Failed to add command " + confName, re);
                }
            }
            ANALYSIS_LOG.info(analysisReport.toString() + " </analyse>");

            LOG.info(INFO_COMMAND_COUNT + commands.size());

            // mark state that we're about to execute the sub threads
            allCancelled = commands.size() > 0;
            boolean hitsToShow = false;


            // DataModel's ControlLevel will be SEARCH_COMMAND_CONSTRUCTION
            //  Increment it onwards to SEARCH_COMMAND_CONSTRUCTION.
            dataModelFactory.assignControlLevel(datamodel, ControlLevel.SEARCH_COMMAND_EXECUTION);

            final Map<Future<ResultList<? extends ResultItem>>,SearchCommand> results
                    = executeSearchCommands(commands);

            // DataModel's ControlLevel will be SEARCH_COMMAND_CONSTRUCTION
            //  Increment it onwards to RUNNING_QUERY_RESULT_HANDLING.
            dataModelFactory.assignControlLevel(datamodel, ControlLevel.RUNNING_QUERY_RESULT_HANDLING);

            if( !allCancelled ){

                final StringBuilder noHitsOutput = new StringBuilder();

                for (Future<ResultList<? extends ResultItem>> task : results.keySet()) {

                    if (task.isDone() && !task.isCancelled()) {

                        try{
                            final ResultList<? extends ResultItem> searchResult = task.get();
                            if (searchResult != null) {

                                // Information we need about and for the enrichment
                                final SearchCommand command = results.get(task);
                                final SearchConfiguration config = command.getSearchConfiguration();

                                final String name = config.getName();
                                final EnrichmentHint eHint = context.getSearchTab().getEnrichmentByCommand(name);

                                final float score = scores.get(name) != null
                                        ? scores.get(name) * eHint.getWeight()
                                        : 0;

                                // update hit status
                                hitsToShow |= searchResult.getHitCount() > 0;
                                hits.put(name, searchResult.getHitCount());

                                if( searchResult.getHitCount() <= 0 && command.isPaginated() ){
                                    noHitsOutput.append("<command id=\"" + config.getName()
                                            + "\" name=\""  + config.getStatisticalName()
                                            + "\" type=\"" + config.getClass().getSimpleName()
                                            + "\"/>");
                                }

                                // score
                                if(eHint != null && searchResult.getHitCount() > 0 && score >= eHint.getThreshold()) {

                                    searchResult.addField(EnrichmentHint.NAME_KEY, name);
                                    searchResult.addObjectField(EnrichmentHint.SCORE_KEY, score);
                                    searchResult.addObjectField(EnrichmentHint.HINT_KEY, eHint);
                                    for(Map.Entry<String,String> property : eHint.getProperties().entrySet()){
                                        searchResult.addObjectField(property.getKey(), property.getValue());
                                    }
                                }
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

    /**
     *
     * @return
     */
    protected Map<String,Integer> getHits(){
        return Collections.unmodifiableMap(hits);
    }

    // Private -------------------------------------------------------

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

            // everything on by default
            boolean applicable = (0 == explicitCommands.length);

            // check for specified list of commands to run in url
            for(String explicitCommand : explicitCommands){
                applicable |= explicitCommand.equalsIgnoreCase(conf.getName());
            }

            // check output is rss, only run the command that will produce the rss output. only disable applicable.
            applicable &= !isRss() || context.getSearchTab().getRssResultName().equals(conf.getName());

            // check for alwaysRun or for a possible enrichment (since its scoring will be the final indicator)
            applicable &= conf.isAlwaysRun() ||
                    (null != context.getSearchTab().getEnrichmentByCommand(conf.getName())
                    && !datamodel.getQuery().getQuery().isBlank());

            // add search configuration if applicable
            if(applicable){
                applicableSearchConfigurations.add(conf);
            }
        }
        
        return performTransformers(applicableSearchConfigurations);
    }

    private boolean useEnrichment(
            final EnrichmentHint eHint,
            final SearchConfiguration config,
            final TokenEvaluationEngineContext tokenEvaluationEngineContext,
            final StringBuilder analysisReport){

        boolean result = false;

        final Map<String,StringDataObject> parameters = datamodel.getParameters().getValues();

        // TODO 'collapse' is not a sesat standard. standardise or move out.
        final boolean collapse = null == parameters.get("collapse")
                || "".equals(parameters.get("collapse").getString());

        if (context.getSearchMode().isAnalysis() && collapse && eHint.getWeight() > 0){

            int score = eHint.getBaseScore();

            if(null != eHint.getRule()){

                final AnalysisRule rule = rules.getRule(eHint.getRule());

                if (null == scoresByRule.get(eHint.getRule())) {

                    final StringBuilder analysisRuleReport = new StringBuilder();

                    score += rule.evaluate(datamodel.getQuery().getQuery(),
                            ContextWrapper.wrap(
                                AnalysisRule.Context.class,
                                new BaseContext(){
                                    public String getRuleName(){
                                        return eHint.getRule();
                                    }
                                    public Appendable getReportBuffer(){
                                        return analysisRuleReport;
                                    }
                                },
                                tokenEvaluationEngineContext));

                    scoresByRule.put(eHint.getRule(), score);
                    analysisReport.append(analysisRuleReport);

                    LOG.debug("Score for " + config.getName() + " is " + score);

                } else {
                    score = scoresByRule.get(eHint.getRule());
                }
            }

            scores.put(config.getName(), score);

            result = score >= eHint.getThreshold();

        }

        return config.isAlwaysRun() || result;
    }

    @SuppressWarnings("unchecked")
    private Map<Future<ResultList<? extends ResultItem>>,SearchCommand> executeSearchCommands(
            final Collection<SearchCommand> commands) throws InterruptedException, TimeoutException, ExecutionException{

        Map<Future<ResultList<? extends ResultItem>>,SearchCommand> results = Collections.EMPTY_MAP;

        try{
            final SearchCommandExecutor executor = SearchCommandExecutorFactory
                    .getController(context.getSearchMode().getExecutor());

            try{
                results = executor.invokeAll(commands);

            }finally{

                final Map<Future<ResultList<? extends ResultItem>>,SearchCommand> waitFor;

                if(null != datamodel.getParameters().getValue(PARAM_WAITFOR)){
                    
                    waitFor = new HashMap<Future<ResultList<? extends ResultItem>>,SearchCommand>();
                    
                    final String[] waitForArr 
                            = datamodel.getParameters().getValue(PARAM_WAITFOR).getString().split(",");
                    
                    for(String waitForStr : waitForArr){
                        // using generics on the next line crashes javac
                        for(Entry/*<Future<ResultList<? extends ResultItem>>,SearchCommand>*/ entry 
                                : results.entrySet()){
                            
                            final String entryName 
                                    = ((SearchCommand)entry.getValue()).getSearchConfiguration().getName();
                            if(waitForStr.equalsIgnoreCase(entryName)){

                                waitFor.put(
                                        (Future<ResultList<? extends ResultItem>>)entry.getKey(),
                                        (SearchCommand)entry.getValue());
                                break;
                            }
                        }
                    }
                    
                }else if(null != datamodel.getParameters().getValue(PARAM_COMMANDS)){
                    
                    // wait on everything explicitly asked for
                    waitFor = results;
                    
                }else{

                    // do not wait on asynchronous commands
                    waitFor = new HashMap<Future<ResultList<? extends ResultItem>>,SearchCommand>();
                    // using generics on the next line crashes javac
                    for(Entry/*<Future<ResultList<? extends ResultItem>>,SearchCommand>*/ entry : results.entrySet()){
                        if(!((SearchCommand)entry.getValue()).getSearchConfiguration().isAsynchronous()){

                            waitFor.put(
                                    (Future<ResultList<? extends ResultItem>>)entry.getKey(),
                                    (SearchCommand)entry.getValue());
                        }
                    }
                }
                executor.waitForAll(waitFor, TIMEOUT);
            }
        }catch(TimeoutException te){
            LOG.error(ERR_MODE_TIMEOUT + te.getMessage());
        }

        // Check that we have atleast one valid execution
        for(SearchCommand command : commands){
            allCancelled &= (null != datamodel.getParameters().getValue(PARAM_COMMANDS)
                    || !command.getSearchConfiguration().isAsynchronous());
            allCancelled &= command.isCancelled();
        }

        return results;
    }

    private Collection<SearchConfiguration> performTransformers(final Collection<SearchConfiguration> applicableSearchConfigurations) {
        final RunTransformer.Context transformerContext = new RunTransformer.Context() {
                    public Collection<SearchConfiguration>getApplicableSearchConfigurations() {
                        return applicableSearchConfigurations;
                    }

                    public DataModel getDataModel() {
                        return datamodel;
                    }

                    public DocumentLoader newDocumentLoader(final SiteContext siteContext, final String resource, final DocumentBuilder builder) {
                        return context.newDocumentLoader(siteContext, resource, builder);
                    }

                    public PropertiesLoader newPropertiesLoader(final SiteContext siteContext, final String resource, final Properties properties) {
                        return context.newPropertiesLoader(siteContext, resource, properties);
                    }
                    
                    public BytecodeLoader newBytecodeLoader(final SiteContext siteContext, final String className, final String jarFileName) {
                        return context.newBytecodeLoader(siteContext, className, jarFileName);
                    }

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

    private void performHandlers(){

        final RunHandler.Context handlerContext = ContextWrapper.wrap(
                RunHandler.Context.class,
                new SiteContext(){
                    public Site getSite() {
                        return datamodel.getSite().getSite();
                    }
                },
                context);

        final List<RunHandlerConfig> rhcList 
                = new ArrayList<RunHandlerConfig>(context.getSearchMode().getRunHandlers());
        
        /* Adding NavigationRunHandler to all search modes */
        rhcList.add(new NavigationRunHandlerConfig());

        for (final RunHandlerConfig rhc : rhcList) {
            final RunHandler rh = RunHandlerFactory.getController(handlerContext, rhc);
            rh.handleRunningQuery(handlerContext);
        }
    }

    // Inner classes -------------------------------------------------
    private boolean isRss() {

        final StringDataObject outputParam = datamodel.getParameters().getValue(PARAM_LAYOUT);
        return null != outputParam && "rss".equals(outputParam.getString());
    }

    private void handleNoHits(final StringBuilder noHitsOutput, final Map<String,StringDataObject> parameters)
            throws SiteKeyedFactoryInstantiationException, InterruptedException{

        // there were no hits for any of the search tabs!
        noHitsOutput.append("<absolute/>");

        if( noHitsOutput.length() >0 && datamodel.getQuery().getString().length() >0
                && !"NOCOUNT".equals(parameters.get("IGNORE"))){

            final String output = null != parameters.get("output")
                    ? parameters.get("output").getString()
                    : null;

            noHitsOutput.insert(0, "<no-hits mode=\"" + context.getSearchTab().getKey()
                    + (null != output ? "\" output=\"" + output : "") + "\">"
                    + "<query>" + datamodel.getQuery().getXmlEscaped() + "</query>");
            noHitsOutput.append("</no-hits>");
            PRODUCT_LOG.info(noHitsOutput.toString());
        }
        
        // maybe we can modify the query to broaden the search
        // replace all DefaultClause with an OrClause
        //  [simply done with wrapping the query string inside ()'s ]
        final String queryStr = datamodel.getQuery().getString();

        if (!queryStr.startsWith("(") && !queryStr.endsWith(")")
                && datamodel.getQuery().getQuery().getTermCount() > 1) {

            // DataModel's ControlLevel will be RUNNING_QUERY_CONSTRUCTION
            //  Increment it onwards to SEARCH_COMMAND_CONSTRUCTION.
            final DataModelFactory dataModelFactory =  DataModelFactory
                    .instanceOf(ContextWrapper.wrap(DataModelFactory.Context.class, context, new SiteContext(){
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

    // Inner classes -------------------------------------------------
}
