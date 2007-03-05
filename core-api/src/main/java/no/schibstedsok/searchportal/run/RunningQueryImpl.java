/*
 * Copyright (2005-2007) Schibsted Søk AS
 *
 */
package no.schibstedsok.searchportal.run;


import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.InfrastructureException;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.DataModelFactory;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.query.QueryDataObject;
import no.schibstedsok.searchportal.query.analyser.AnalysisRule;
import no.schibstedsok.searchportal.query.analyser.AnalysisRuleFactory;
import no.schibstedsok.searchportal.query.QueryStringContext;
import no.schibstedsok.searchportal.query.token.ReportingTokenEvaluator;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.searchportal.query.token.TokenMatch;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.mode.SearchCommandFactory;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.parser.QueryParser;
import no.schibstedsok.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.searchportal.query.token.VeryFastListQueryException;
import no.schibstedsok.searchportal.result.Enrichment;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.schibstedsok.searchportal.site.config.SiteConfiguration;
import no.schibstedsok.searchportal.util.Channels;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.view.i18n.TextMessages;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * An object representing a running queryStr.
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

    private static final String PARAM_OUTPUT = "output";

    private static final Logger LOG = Logger.getLogger(RunningQueryImpl.class);
    private static final Logger ANALYSIS_LOG = Logger.getLogger("no.schibstedsok.searchportal.analyzer.Analysis");
    private static final Logger PRODUCT_LOG = Logger.getLogger("no.schibstedsok.Product");

    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";
    private static final String ERR_RUN_QUERY = "Failure to run query";
    private static final String ERR_EXECUTION_ERROR = "Failure on a search command: ";
    private static final String INFO_COMMAND_COUNT = "Commands to invoke ";

    // Attributes ----------------------------------------------------

    private final AnalysisRuleFactory rules;

    /** Map of search command IDs to SearchResult */
    private final Map<String, Future<SearchResult>> results = new HashMap<String,Future<SearchResult>>();

    /** Mutext for results variable */
    private final ReadWriteLock resultsLock = new ReentrantReadWriteLock();

    /** have all search commands been cancelled.
     * implementation details allowing web subclasses to send correct error to client. **/
    protected boolean allCancelled = false;
    /** TODO comment me. **/
    protected final DataModel datamodel;
    private final Locale locale;
    private final List<Modifier> sources = new Vector<Modifier>();
    /** TODO comment me. **/
    protected final TokenEvaluationEngine engine;
    private final List<Enrichment> enrichments = new ArrayList<Enrichment>();
    private final Map<String,Integer> hits = new HashMap<String,Integer>();
    private final Map<String,Integer> scores = new HashMap<String,Integer>();
    private final Map<String,Integer> scoresByRule = new HashMap<String,Integer>();

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Create a new Running Query instance.
     *
     * @param mode
     * @param queryStr
     * @param parameters
     */
    public RunningQueryImpl(
            final Context cxt, 
            final String query, 
            final DataModel datamodel) throws SiteKeyedFactoryInstantiationException {

        super(cxt);
        
        final Map<String,Object> parameters = datamodel.getJunkYard().getValues();

        LOG.trace("RunningQuery(cxt," + query + "," + parameters + ")");

        final String queryStr = trimDuplicateSpaces(query);

        locale = cxt.getSite().getLocale();

        this.datamodel = datamodel;
        initParameters(cxt);

        final TokenEvaluationEngine.Context tokenEvalFactoryCxt =
                ContextWrapper.wrap(
                    TokenEvaluationEngine.Context.class,
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return queryStr;
                        }
                    });

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
                = DataModelFactory.valueOf(ContextWrapper.wrap(DataModelFactory.Context.class, cxt));

        final QueryDataObject queryDO = factory.instantiate(
                QueryDataObject.class,
                new DataObject.Property("string", queryStr),
                new DataObject.Property("query", parser.getQuery()));
        
        datamodel.setQuery(queryDO);
        
        rules = AnalysisRuleFactory.valueOf(ContextWrapper.wrap(AnalysisRuleFactory.Context.class, context));

    }

    // Public --------------------------------------------------------


    /** {@inherit}. **/
    public List<TokenMatch> getGeographicMatches() {


        final List<TokenMatch> matches = new ArrayList<TokenMatch>();

        try{

            matches.addAll(getTokenMatches(TokenPredicate.GEOLOCAL));
            matches.addAll(getTokenMatches(TokenPredicate.GEOGLOBAL));

        }catch(VeryFastListQueryException ie){
            LOG.error("Magnus: fix SEARCH-943   :-)");
        }
        Collections.sort(matches);

        return matches;
    }

    /** {@inherit}. **/
    public String getGlobalSearchTips () {

        LOG.trace("getGlobalSearchTips()");
        return null;
    }


    /** {@inherit}. **/
    public Integer getNumberOfHits(final String configName) {

        LOG.trace("getNumberOfHits(" + configName + ")");
        return hits.get(configName) != null ? hits.get(configName) : Integer.valueOf(0);
    }

    /** {@inherit}. **/
    public Map<String,Integer> getHits(){
        return Collections.unmodifiableMap(hits);
    }

    /** {@inheritDoc} */
    public final SearchResult getSearchResult(final String id) throws InterruptedException, ExecutionException {

        final Future<SearchResult> task;
        try {
            resultsLock.readLock().lock();
            if (!results.containsKey(id)) {
                return null;
            }
            task = results.get(id);
        } finally {
            resultsLock.readLock().unlock();
        }

        return task.get();
    }

    /**
     * Thread run. Guts of the logic behind this class.
     * XXX Insanely long method. Divide & Conquer.
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {

        LOG.trace("run()");
        final StringBuilder analysisReport 
                = new StringBuilder(" <analyse><query>" + datamodel.getQuery().getXmlEscaped() + "</query>\n");
        
        final Map<String,Object> parameters = datamodel.getJunkYard().getValues();

        try {

            final Collection<Callable<SearchResult>> commands = new ArrayList<Callable<SearchResult>>();

            final boolean isRss = parameters.get(PARAM_OUTPUT) != null && parameters.get(PARAM_OUTPUT).equals("rss");

            for (SearchConfiguration searchConfiguration : context.getSearchMode().getSearchConfigurations()) {

                final SearchConfiguration config = searchConfiguration;
                final String configName = config.getName();

                try{

                    // If output is rss, only run the one command that will produce the rss output.
                    if (!isRss || context.getSearchTab().getRssResultName().equals(configName)) {

                        hits.put(config.getName(), Integer.valueOf(0));

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

                        final SearchTab.EnrichmentHint eHint = context.getSearchTab().getEnrichmentByCommand(configName);

                        if (eHint != null && !datamodel.getQuery().getQuery().isBlank()) {

                            final AnalysisRule rule = rules.getRule(eHint.getRule());

                            if (context.getSearchMode().isAnalysis()
                                    && "0".equals(parameters.get("offset"))
                                    && eHint.getWeight() > 0) {

                                int score = 0;

                                if (null == scoresByRule.get(eHint.getRule())) {

                                    final StringBuilder analysisRuleReport = new StringBuilder();

                                    score = rule.evaluate(datamodel.getQuery().getQuery(), 
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
                                                searchCmdCxt));

                                    scoresByRule.put(eHint.getRule(), score);
                                    analysisReport.append(analysisRuleReport);

                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Score for " + searchConfiguration.getName() + " is " + score);
                                    }

                                } else {
                                    score = scoresByRule.get(eHint.getRule());
                                }

                                scores.put(config.getName(), score);

                                if (config.isAlwaysRun() || score >= eHint.getThreshold()) {
                                    commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, datamodel));
                                }

                            } else if (config.isAlwaysRun()) {
                                commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, datamodel));
                            }

                        } else {

                            commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, datamodel));
                        }
                    }
                }catch(RuntimeException re){
                    LOG.error("Failed to add command " + configName, re);
                }
            }
            ANALYSIS_LOG.info(analysisReport.toString() + " </analyse>");

            LOG.info(INFO_COMMAND_COUNT + commands.size());

            // mark state that we're about to execute the sub threads
            allCancelled = commands.size() > 0;

            /* Entering CS */
            try {
                resultsLock.writeLock().lock();
                context.getSearchMode().getExecutor().invokeAll(commands, results, TIMEOUT);
            } finally {
                /* Leaving CS */
                resultsLock.writeLock().unlock();
            }

            // TODO This loop-(task.isDone()) code should become individual listeners to each executor to minimise time
            //  spent in task.isDone()
            boolean hitsToShow = false;

            /* Give the commands a chance to finish its work */
            for (Future<SearchResult> task : results.values()) {
                try{
                    task.get(TIMEOUT, TimeUnit.MILLISECONDS);
                }catch(TimeoutException te){
                    LOG.error(ERR_EXECUTION_ERROR, te);
                }
            }

            // Ensure any cancellations are properly handled
            for(Callable<SearchResult> command : commands){
                allCancelled &= ((SearchCommand)command).handleCancellation();
            }

            if( !allCancelled ){

                final StringBuilder noHitsOutput = new StringBuilder();

                for (Future<SearchResult> task : results.values()) {

                    if (task.isDone() && !task.isCancelled()) {

                        try{
                            final SearchResult searchResult = task.get();
                            if (searchResult != null) {

                                // Information we need about and for the enrichment
                                final SearchConfiguration config
                                        = searchResult.getSearchCommand().getSearchConfiguration();

                                final String name = config.getName();
                                final SearchTab.EnrichmentHint eHint
                                        = context.getSearchTab().getEnrichmentByCommand(name);

                                final float score = scores.get(name) != null
                                        ? scores.get(name) * eHint.getWeight()
                                        : 0;

                                // update hit status
                                hitsToShow |= searchResult.getHitCount() > 0;
                                hits.put(name, searchResult.getHitCount());

                                if( searchResult.getHitCount() <= 0 && config.isPaging() ){
                                    noHitsOutput.append("<command id=\"" + config.getName()
                                            + "\" name=\""  + config.getStatisticalName()
                                            + "\" type=\"" + searchResult.getSearchCommand().getClass().getSimpleName()
                                            + "\"/>");
                                }

                                // score
                                if(eHint != null && searchResult.getHitCount() > 0 && score >= eHint.getThreshold()) {

                                    // add enrichment
                                    final Enrichment e = new Enrichment(score, name);
                                    enrichments.add(e);
                                }
                            }
                        }catch(ExecutionException ee){
                            LOG.error(ERR_EXECUTION_ERROR, ee);
                        }
                    }
                }

                performModifierHandling();

                if (!hitsToShow) {
                    noHitsOutput.append("<absolute/>");
    // FIXME: i do not know how to reset/clean the sitemesh's outputStream so
    //                  the result from the new RunningQuery are used.
    //                int sourceHits = 0;
    //                for (final Iterator it = sources.iterator(); it.hasNext();) {
    //                    sourceHits += ((Modifier) it.next()).getCount();
    //                }
    //                if (sourceHits == 0) {
    //                    // there were no hits for any of the search tabs!
    //                    // maybe we can modify the query to broaden the search
    //                    // replace all DefaultClause with an OrClause
    //                    //  [simply done with wrapping the query string inside ()'s ]
    //                    if (!queryStr.startsWith("(") && !queryStr.endsWith(")") && queryObj.getTermCount() > 1) {
    //                        // create and run a new RunningQueryImpl
    //                        new RunningQueryImpl(context, '(' + queryStr + ')', parameters).run();
    //                    }
    //                }
                }  else  {

                    performEnrichmentHandling(results.values());
                }

                if( noHitsOutput.length() >0 && datamodel.getQuery().getString().length() >0 && !"NOCOUNT".equals(parameters.get("IGNORE"))){
                    final String output = (String) parameters.get("output");

                    noHitsOutput.insert(0, "<no-hits mode=\"" + context.getSearchTab().getKey()
                            + (null != output ? "\" output=\"" + output : "") + "\">"
                            + "<query>" + datamodel.getQuery().getXmlEscaped() + "</query>");
                    noHitsOutput.append("</no-hits>");
                    PRODUCT_LOG.info(noHitsOutput.toString());
                }
            }

        } catch (Exception e) {
            LOG.error(ERR_RUN_QUERY, e);
            throw new InfrastructureException(e);

        }
    }


    /** TODO comment me. **/
    protected void addParameter(final String key, final Object obj) {
        datamodel.getJunkYard().getValues().put(key, obj);
    }

    /** {@inherit}. **/
    public Locale getLocale() {

        LOG.trace("getLocale()");

        return locale;
    }

    /** {@inherit}. **/
    public SearchMode getSearchMode() {

        LOG.trace("getSearchMode()");

        return context.getSearchMode();
    }

    /** {@inherit}. **/
    public SearchTab getSearchTab(){

        LOG.trace("getSearchTab()");

        return context.getSearchTab();
    }

    /** {@inherit}. **/
    public List<Modifier> getSources() {

        LOG.trace("getSources()");

        return sources;
    }

    /** {@inherit}. **/
    public void addSource(final Modifier modifier) {

        LOG.trace("addSource()");

        sources.add(modifier);
    }

    /** {@inherit}. **/
    public List<Enrichment> getEnrichments() {

        LOG.trace("getEnrichments()");

        return enrichments;
    }

    /** {@inherit}. **/
    public Query getQuery() {
        return datamodel.getQuery().getQuery();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private List<TokenMatch> getTokenMatches(final TokenPredicate token) throws VeryFastListQueryException {

        final ReportingTokenEvaluator e = (ReportingTokenEvaluator) engine.getEvaluator(token);
        return e.reportToken(token, datamodel.getQuery().getString());
    }

    private void performEnrichmentHandling(final Collection<Future<SearchResult>> results)
            throws InterruptedException{

        Collections.sort(enrichments);

        final StringBuilder log = new StringBuilder();

        log.append("<enrichments mode=\"" + context.getSearchTab().getKey()
                + "\" size=\"" + enrichments.size() + "\">"
                + "<query>" + datamodel.getQuery().getXmlEscaped() + "</query>");

        /* Write product log and find webtv and tv enrichments */
        for(Enrichment e : enrichments){
            log.append("<enrichment name=\"" + e.getName()
                    + "\" score=\"" + e.getAnalysisResult() + "\"/>");
        }
        log.append("</enrichments>");
        PRODUCT_LOG.info(log.toString());

    }

    /** Remove modifiers with invalid count.
     * Sum duplicates together.
     * Sort by HintPriorityComparator.
     **/
    private void performModifierHandling(){

        final Map<String,Modifier> map = new HashMap<String,Modifier>();
        final List<Modifier> toRemove = new ArrayList<Modifier>();
        for(Modifier m : sources){
            if(m.getCount() > -1 ){
                final Modifier prior = map.get(m.getName());
                if( null == prior ){
                    m.setNavigationHint(context.getSearchTab().getNavigationHint(m.getName()));
                    map.put(m.getName(), m);
                }else{
                    prior.addCount(m.getCount());
                    toRemove.add(m);
                }
            }else{
                toRemove.add(m);
            }
        }
        sources.removeAll(toRemove);

        if (getSearchTab().getAbsoluteOrdering()) {
            Collections.sort(sources, Modifier.getHintPriorityComparator());
        } else {
            Collections.sort(sources);
        }
    }

    /** Used by the constructor. **/
    private void initParameters(final RunningQuery.Context rqCxt){

        final Map<String,Object> parameters = datamodel.getJunkYard().getValues();
        
        parameters.put("query", this);
        parameters.put("locale", locale);
        if( null == parameters.get("offset") ){
            parameters.put("offset", "0");
        }

        final Properties props = SiteConfiguration.valueOf(
                        ContextWrapper.wrap(SiteConfiguration.Context.class, rqCxt)).getProperties();

        parameters.put("configuration", props);
        parameters.put("text", TextMessages.valueOf(ContextWrapper.wrap(TextMessages.Context.class, rqCxt)));
        parameters.put("channels", Channels.valueOf(ContextWrapper.wrap(Channels.Context.class, rqCxt)));

        parameters.put("tab", rqCxt.getSearchTab());
        parameters.put("c", rqCxt.getSearchTab().getKey());
    }

    // Inner classes -------------------------------------------------
}
