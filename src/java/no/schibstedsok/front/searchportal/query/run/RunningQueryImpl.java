/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.query.run;


import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.QueryTokenizer;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.query.analyser.AnalysisRule;
import no.schibstedsok.front.searchportal.query.analyser.AnalysisRuleFactory;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.front.searchportal.query.token.ReportingTokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.query.token.TokenMatch;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.impl.SearchCommandFactory;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.parser.QueryParser;
import no.schibstedsok.front.searchportal.result.Enrichment;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import no.schibstedsok.front.searchportal.view.config.SearchTab;
import no.schibstedsok.front.searchportal.view.output.VelocityResultHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * An object representing a running queryStr.
 *
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class RunningQueryImpl extends AbstractRunningQuery implements RunningQuery {

    private static final Logger LOG = Logger.getLogger(RunningQueryImpl.class);
    private static final Logger ANALYSIS_LOG = Logger.getLogger("no.schibstedsok.front.searchportal.analyzer.Analysis");
    private static final Logger PRODUCT_LOG = Logger.getLogger("no.schibstedsok.Product");

    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";
    private static final String ERR_RUN_QUERY = "Failure to run query";

    private final AnalysisRuleFactory rules;
    private final String queryStr;
    private final Query queryObj;
    private final Map parameters = new HashMap();;
    private int offset;
    private Locale locale;
    private final List<Modifier> sources = new Vector<Modifier>();
    private final TokenEvaluatorFactory tokenEvaluatorFactory;
    private final List<Enrichment> enrichments = new ArrayList<Enrichment>();
    private final Map<String,Integer> hits = new HashMap<String,Integer>();
    private final Map<String,Integer> scores = new HashMap<String,Integer>();


    /**
     * Create a new Running Query instance.
     *
     * @param mode
     * @param queryStr
     * @param parameters
     */
    public RunningQueryImpl(final Context cxt, final String query, final Map parameters) {

        super(cxt);

        LOG.trace("RunningQuery(cxt," + query + "," + parameters + ")");

        queryStr = trimDuplicateSpaces(query);

        this.parameters.putAll(parameters);
        this.locale = new Locale("no", "NO");

        final TokenEvaluatorFactoryImpl.Context tokenEvalFactoryCxt =
                ContextWrapper.wrap(
                    TokenEvaluatorFactoryImpl.Context.class,
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return RunningQueryImpl.this.getQueryString();
                        }
                    });

        // This will among other things perform the initial fast search
        // for textual analysis.
        tokenEvaluatorFactory = new TokenEvaluatorFactoryImpl(tokenEvalFactoryCxt);

        // queryStr parser
        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
            public TokenEvaluatorFactory getTokenEvaluatorFactory() {
                return tokenEvaluatorFactory;
            }
        });

        queryObj = parser.getQuery();

        rules = AnalysisRuleFactory.valueOf(ContextWrapper.wrap(AnalysisRuleFactory.Context.class, context));

    }

    private List<TokenMatch> getTokenMatches(final String token) {
        final ReportingTokenEvaluator e
                = (ReportingTokenEvaluator) tokenEvaluatorFactory.getEvaluator(TokenPredicate.valueFor(token));
        return e.reportToken(token, queryStr);
    }

    public List<TokenMatch> getGeographicMatches() {
        final List<TokenMatch> matches = new ArrayList<TokenMatch>();

        matches.addAll(getTokenMatches("geolocal"));
        matches.addAll(getTokenMatches("geoglobal"));

        Collections.sort(matches);

        return matches;
    }

    /**
     * First find out if the user types in an advanced search etc by analyzing the queryStr.
     * Then lookup correct tip using messageresources.
     *
     * @return user tip
     */
    public String getGlobalSearchTips () {

        LOG.trace("getGlobalSearchTips()");
        return null;
    }


    public Integer getNumberOfHits(final String configName) {

        LOG.trace("getNumberOfHits(" + configName + ")");

        Integer i = hits.get(configName);
        if (i == null) {
            i = Integer.valueOf(0);
        }
        return i;
    }

    /**
     * Thread run
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {

        LOG.trace("run()");
        ANALYSIS_LOG.info("<analyse><query>" + queryStr + "</query>");

        try {

            final Collection<Callable<SearchResult>> commands = new ArrayList<Callable<SearchResult>>();

            for (SearchConfiguration searchConfiguration : context.getSearchMode().getSearchConfigurations()) {

                final SearchConfiguration config = searchConfiguration;
                final String configName = config.getName();

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
                                return queryObj;
                            }
                        }
                );

                final SearchTab.EnrichmentHint eHint = context.getSearchTab().getEnrichmentByCommand(configName);
                if(eHint != null && !queryObj.isBlank()){
                    final AnalysisRule rule = rules.getRule(eHint.getRule());

                    if (context.getSearchMode().isQueryAnalysisEnabled() && offset == 0) {

                        LOG.debug("run: searchMode.getKey().equals(d) && offset == 0");

                        ANALYSIS_LOG.info(" <analysis name=\"" + eHint.getRule() + "\">");
                        LOG.debug("Scoring new style for " + eHint.getRule());
                        final int newScore = rule.evaluate(queryObj, tokenEvaluatorFactory);

                        LOG.debug("Score for " + searchConfiguration.getName() + " is " + newScore);

                        if(newScore != 0){
                            ANALYSIS_LOG.info("  <score>" + newScore + "</score>");
                        }
                        ANALYSIS_LOG.info(" </analysis>");

                        scores.put(config.getName(), Integer.valueOf(newScore));

                        if (config.isAlwaysRunEnabled() || newScore >= eHint.getThreshold()) {
                            commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
                        }

                    } else if (config.isAlwaysRunEnabled()) {
                        commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
                    }

                } else {

//                    // Optimisation. Alternate between the two web searches.
//                    if (isNorwegian(config) || isInternational(config)) {
//                        final String searchType = getSingleParameter("s");
//                        if (searchType != null && searchType.equals("g")) {
//                            if (isInternational(config)) {
//                                commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
//                            }
//                        } else if (isNorwegian(config)) {
//                            commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
//                        }
//                    } else {
                        commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
//                    }
                }
            }

            ANALYSIS_LOG.info("</analyse>");

            LOG.debug("run(): InvokeAll Commands.size=" + commands.size());

            final List<Future<SearchResult>> results = context.getSearchMode().getExecutor().invokeAll(commands,
                    Logger.getRootLogger().getLevel().isGreaterOrEqual(Level.INFO) ?  10000 :  Integer.MAX_VALUE);

            // TODO This loop-(task.isDone()) code should become individual listeners to each executor to minimise time
            //  spent in task.isDone()
            boolean hitsToShow = false;

            // Ensure any cancellations are properly handled
            for(Callable<SearchResult> command : commands){
                ((SearchCommand)command).handleCancellation();
            }

            for (Future<SearchResult> task : results) {

                if (task.isDone() && !task.isCancelled()) {

                    final SearchResult searchResult = task.get();
                    if (searchResult != null) {

                        // Information we need about and for the enrichment
                        final SearchConfiguration config = searchResult.getSearchCommand().getSearchConfiguration();
                        final String name = config.getName();
                        final SearchTab.EnrichmentHint eHint = context.getSearchTab().getEnrichmentByCommand(name);
                        final float score = scores.get(name) != null
                                ? scores.get(name) * eHint.getWeight()
                                : 0;

                        // update hit status
                        hitsToShow |= searchResult.getHitCount() > 0;
                        hits.put(name, searchResult.getHitCount());

                        // score
                        if(eHint != null && searchResult.getResults().size() > 0
                                && score >= eHint.getThreshold() && score > 15) {

                            // add enrichment
                            final Enrichment e = new Enrichment(score, name);
                            enrichments.add(e);
                        }
                    }
                }
            }

            Collections.sort(sources);

            if (!hitsToShow) {
                PRODUCT_LOG.info("<no-hits mode=\"" + context.getSearchTab().getKey() + "\">"
                        + "<query>" + queryStr + "</query></no-hits>");
// FIXME: i do not know how to reset/clean the sitemesh's outputStream so the result from the new RunningQuery are used.
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

                performEnrichmentHandling(results);
            }
        } catch (Exception e) {
            LOG.error(ERR_RUN_QUERY, e);
        }
    }

    private void performEnrichmentHandling(final List<Future<SearchResult>> results) throws InterruptedException, ExecutionException {

        Collections.sort(enrichments);

        PRODUCT_LOG.info("<enrichments mode=\"" + context.getSearchTab().getKey()
                + "\" size=\"" + enrichments.size() + "\">"
                + "<query>" + queryStr + "</query>");

        Enrichment tvEnrich = null;
        Enrichment webtvEnrich = null;

        /* Write product log and find webtv and tv enrichments */
        for(Enrichment e : enrichments){
            PRODUCT_LOG.info("  <enrichment name=\"" + e.getName()
                    + "\" score=\"" + e.getAnalysisResult() + "\"/>");

            /* Store reference to webtv and tv enrichments */
            if ("webtvEnrich".equals(e.getName())) {
                webtvEnrich = e;
            } else if ("tvEnrich".equals(e.getName())) {
                tvEnrich = e;
            }
        }
        PRODUCT_LOG.info("</enrichments>");
        
        SearchResult tvResult = null;
        SearchResult webtvResult = null;

        /* Find webtv and tv results */
        for (Future<SearchResult> task : results) {
            if (task.isDone() && !task.isCancelled()) {
                SearchResult sr = task.get();
                if ("webtvEnrich".equals(sr.getSearchCommand().getSearchConfiguration().getName())) {
                    webtvResult = sr;
                } else if ("tvEnrich".equals(sr.getSearchCommand().getSearchConfiguration().getName())) {
                    tvResult = sr;
                }
            }
        }
        
        /* Update score and if necessary the enrichment name */
        if (webtvEnrich != null && tvEnrich != null) {
            if (webtvEnrich.getAnalysisResult() > tvEnrich.getAnalysisResult()) {
                tvEnrich.setAnalysisResult(webtvEnrich.getAnalysisResult());
            }
            enrichments.remove(webtvEnrich);
        } else if (webtvEnrich != null && tvEnrich == null) {
            tvEnrich = webtvEnrich;
            webtvEnrich.setName("tvEnrich");
        }
        
        /* Merge webtv results into tv results */
        if (webtvResult != null && webtvResult.getResults().size() > 0) {
            if (tvResult != null) {
                tvResult.getResults().addAll(webtvResult.getResults());
                tvResult.setHitCount(tvResult.getHitCount() + webtvResult.getHitCount());
            }
        }
        
        /* Run velocity result handler on the enrichment results */
        if (tvResult != null && tvResult.getResults().size() > 0) {
            final VelocityResultHandler vrh = new VelocityResultHandler();
            final SearchResult cxtResult = tvResult;

            final ResultHandler.Context resultHandlerContext = ContextWrapper.wrap(
                    ResultHandler.Context.class,
                    new BaseContext(){
                        public SearchResult getSearchResult() {
                            return cxtResult;
                        }
                        
                        public SearchTab getSearchTab(){
                            return RunningQueryImpl.this.getSearchTab();
                        }
                        
                        /** @deprecated implementations should be using the QueryContext instead! */
                        public String getQueryString() {
                            return queryObj.getQueryString();
                        }

                        public Query getQuery() {
                            return queryObj;
                        }
                    },
                    context
            );
            vrh.handleResult(resultHandlerContext, parameters);
        }
    }

    private String getSingleParameter(final String paramName) {

        LOG.trace("getSingleParameter()");

        final String[] param = (String[]) parameters.get(paramName);

        return (param != null) ? param[0] : null;
    }

    private boolean isInternational(final SearchConfiguration searchConfiguration) {
        return "globalSearch".equals(searchConfiguration.getName());
    }

    private boolean isNorwegian(final SearchConfiguration searchConfiguration) {
        return "defaultSearch".equals(searchConfiguration.getName());
    }

    protected void addParameter(final String key, final Object obj) {
        parameters.put(key, obj);
    }

    public int getNumberOfTerms() {

        LOG.trace("getNumberOfTerms()");

        return QueryTokenizer.tokenize(queryStr).size();
    }


    public String getQueryString() {

        LOG.trace("getQueryString()");

        return queryStr;
    }

    public int getOffset() {

        LOG.trace("getOffset(): " + offset);

        return offset;
    }

    public void setOffset(final int offset) {
        LOG.trace("setOffset():" + offset);

        this.offset = offset;
    }

    public Locale getLocale() {

        LOG.trace("getLocale()");

        return locale;
    }

    public SearchMode getSearchMode() {

        LOG.trace("getSearchMode()");

        return context.getSearchMode();
    }

    public SearchTab getSearchTab(){

        LOG.trace("getSearchTab()");

        return context.getSearchTab();
    }

    public List<Modifier> getSources() {

        LOG.trace("getSources()");

        return sources;
    }

    public void addSource(final Modifier modifier) {

        LOG.trace("addSource()");

        sources.add(modifier);
    }

    public List<Enrichment> getEnrichments() {

        LOG.trace("getEnrichments()");

        return enrichments;
    }

    public TokenEvaluatorFactory getTokenEvaluatorFactory() {

        LOG.trace("getTokenEvaluatorFactory()");

        return tokenEvaluatorFactory;
    }

    public Query getQuery() {
        return queryObj;
    }
}
