/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.query.run;


import edu.emory.mathcs.backport.java.util.concurrent.CancellationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.QueryTokenizer;
import no.schibstedsok.front.searchportal.analyzer.AnalysisRule;
import no.schibstedsok.front.searchportal.analyzer.AnalysisRuleFactory;
import no.schibstedsok.front.searchportal.configuration.SearchConfigurationContext;
import no.schibstedsok.front.searchportal.query.QueryContext;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.query.parser.TokenMgrError;
import no.schibstedsok.front.searchportal.query.token.ReportingTokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.impl.SearchCommandFactory;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.executor.SearchTask;
import no.schibstedsok.front.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.parser.QueryParser;
import no.schibstedsok.front.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.front.searchportal.query.parser.ParseException;
import no.schibstedsok.front.searchportal.query.token.VeryFastTokenEvaluator;
import no.schibstedsok.front.searchportal.result.Enrichment;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
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

    private static final String ERR_PARSING = "Unable to create RunningQuery's query due to ParseException";

    private final AnalysisRuleFactory rules;
    private String queryStr = "";
    private Query queryObj = null;
    private Map parameters;
    private int offset;
    private Locale locale;
    private final List sources = new ArrayList();
    private final TokenEvaluatorFactory tokenEvaluatorFactory;
    private final List enrichments = new ArrayList();
    private final Map hits = new HashMap();
    private Map scores = new HashMap();


    /**
     * Create a new Running Query instance.
     *
     * @param mode
     * @param queryStr
     * @param parameters
     */
    public RunningQueryImpl(final Context cxt, final String query, final Map parameters) {

        super(cxt);

        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("RunningQuery(cxt," + query + "," + parameters + ")");
        } //</editor-fold>


        queryStr = trimDuplicateSpaces(query);

        if (queryStr != null) {
            queryStr = queryStr.trim();
        }

        this.parameters = parameters;
        this.locale = new Locale("no", "NO");

        final TokenEvaluatorFactoryImpl.Context tokenEvalFactoryCxt = (TokenEvaluatorFactoryImpl.Context) ContextWrapper.wrap(
                TokenEvaluatorFactoryImpl.Context.class,
                new BaseContext[]{
                    context,
                    new QueryStringContext() {
                        public String getQueryString() {
                            return RunningQueryImpl.this.getQueryString();
                        }
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

        try  {
            queryObj = parser.getQuery();
        } catch (ParseException ex)  {
            LOG.error(ERR_PARSING, ex);
        } catch (TokenMgrError ex)  {
            // Errors (as opposed to exceptions) are fatal.
            LOG.fatal(ERR_PARSING, ex);
        }

        rules = AnalysisRuleFactory.valueOf(
                (AnalysisRuleFactory.Context) ContextWrapper.wrap(
                    AnalysisRuleFactory.Context.class,
                    new BaseContext[]{context}));

    }

    private List getTokenMatches(final String token) {
        final ReportingTokenEvaluator e
                = (ReportingTokenEvaluator) tokenEvaluatorFactory.getEvaluator(TokenPredicate.valueOf(token));
        return e.reportToken(token, queryStr);
    }

    public List getGeographicMatches() {
        final List matches = new ArrayList();

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
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled())  {
            LOG.trace("getGlobalSearchTips()");
        } //</editor-fold>
//        if (AdvancedQueryBuilder.isAdvancedQuery(queryStr)) {
//            return TextMessages.valueOf(
//                (TextMessages.Context) ContextWrapper.wrap(
//                    TextMessages.Context.class,
//                    new BaseContext[]{context})).getMessage("searchtip.use+-");
//        } else {
            return null;
//        }
    }


    public Integer getNumberOfHits(final String configName) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getNumberOfHits()");
        } //</editor-fold>
        Integer i = (Integer) hits.get(configName);
        if (i == null) { i = new Integer(0); }
        return i;
    }

    /**
     * Thread run
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("run()");
        } //</editor-fold>
        try {

            final Collection commands = new ArrayList();

            ANALYSIS_LOG.info("<analyse><query>" + queryStr + "</query>");

            for (final Iterator iterator = context.getSearchMode().getSearchConfigurations().iterator(); iterator.hasNext();) {
                final SearchConfiguration searchConfiguration = (SearchConfiguration) iterator.next();

                final SearchCommand.Context searchCmdCxt = (SearchCommand.Context) ContextWrapper.wrap(
                        SearchCommand.Context.class,
                        new BaseContext[]{
                            context,
                            new SearchConfigurationContext() {
                                public SearchConfiguration getSearchConfiguration() {
                                    return searchConfiguration;
                                }
                            },
                            new RunningQueryContext() {
                                public RunningQuery getRunningQuery() {
                                    return RunningQueryImpl.this;
                                }
                            },
                            new QueryContext() {
                                public Query getQuery() {
                                    return queryObj;
                                }
                            }
                });

                final AnalysisRule rule = rules.getRule(searchConfiguration.getRule());

                if (rule != null) {

                    //if (context.getSearchMode().getKey().equals("d") && offset == 0) {
                    if (context.getSearchMode().isQueryAnalysisEnabled() && offset == 0) {

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("run: searchMode.getKey().equals(d) && offset == 0");
                        }
                        
                        ANALYSIS_LOG.info(
                                " <analysis name=\"" + searchConfiguration.getRule() + "\">");

                        LOG.debug("Scoring old style for " + searchConfiguration.getRule());
                        final int oldScore = rule.evaluate(queryStr, tokenEvaluatorFactory);
                        LOG.debug("Scoring new style for " + searchConfiguration.getRule());
                        final int newScore = rule.evaluate(queryObj, tokenEvaluatorFactory);


                        assert (oldScore == newScore); // if this fails, goto mick, do not pass go, do not collect $200.
                        if (oldScore != newScore) {
                            LOG.fatal("\n\n!!! Old score does not match new score !!!\n!!! Query was " + queryStr + "\n");
                            LOG.fatal("OldScore: " + oldScore + "; NewScore: " + newScore + "; for " + searchConfiguration.getRule());
                        }

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Score for " + searchConfiguration.getName() + " is " + newScore);
                        }
                        if(newScore != 0 ){ ANALYSIS_LOG.info("  <score>" + newScore + "</score>"); }
                        ANALYSIS_LOG.info(" </analysis>");

                        scores.put(searchConfiguration.getName(), new Integer(newScore));

                        if (searchConfiguration.isAlwaysRunEnabled() || newScore >= searchConfiguration.getRuleThreshold()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Adding " + searchConfiguration.getName());
                            }
                            commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
                        }

                    } else if (searchConfiguration.isAlwaysRunEnabled()) {
                        commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
                    }
                } else {
                    // Optimisation. Alternate between the two web searches.
                    if (isNorwegian(searchConfiguration) || isInternational(searchConfiguration)) {
                        final String searchType = getSingleParameter("s");
                        if (searchType != null && searchType.equals("g")) {
                            if (isInternational(searchConfiguration)) {
                                commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
                            }
                        } else if (isNorwegian(searchConfiguration)) {
                            commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
                        }
                    } else {
                        commands.add(SearchCommandFactory.createSearchCommand(searchCmdCxt, parameters));
                    }
                }
            }

            ANALYSIS_LOG.info("</analyse>");

            if (LOG.isDebugEnabled()) {
                // [HACK] we using the assumption that EXACTFIRST is a FastTokenPredicate so we can directly access
                //  the VeryFastTokenEvaluator for debugging purposes.
                final VeryFastTokenEvaluator vfte =
                        (VeryFastTokenEvaluator) tokenEvaluatorFactory.getEvaluator(TokenPredicate.EXACTFIRST);
                final Set/*<String>*/ untouchedFastTokens = vfte.getUntouchedTokens();
                if (untouchedFastTokens.size() > 0) {
                    LOG.debug("Listing untouched VeryFast Tokens... (All VeryFast Tokens remain untouched if clauses are WeakReference cached)");
                    for (final Iterator it = untouchedFastTokens.iterator(); it.hasNext();) {
                        LOG.debug("Untouched VeryFast Token -> " + it.next());
                    }
                }

                // Number of commands about to execute.
                LOG.debug("run(): InvokeAll Commands.size=" + commands.size());
            }

            final List results = context.getSearchMode().getExecutor().invokeAll(commands, 30000);

            // TODO This loop-(task.isDone()) code should become individual listeners to each executor to minimise time
            //  spent in task.isDone()
            boolean hitsToShow = false;
            for (final Iterator iterator = results.iterator(); iterator.hasNext();) {
                final SearchTask task = (SearchTask) iterator.next();

                final SearchCommand command = task.getCommand();
                final SearchConfiguration configuration = command.getSearchConfiguration();

                if (task.isDone()) {
                    try {
                        final SearchResult searchResult = (SearchResult) task.get();

                        if (searchResult != null) {
                            hitsToShow |= searchResult.getHitCount()>0;
                            
                            hits.put(configuration.getName(), new Integer(searchResult.getHitCount()));

                            final Integer score = (Integer) scores.get(task.getCommand().getSearchConfiguration().getName());

                            if (score != null && configuration.getRule() != null && score.intValue() >= task.getCommand().getSearchConfiguration().getRuleThreshold()) {
                                if (searchResult.getResults().size() > 0 && score.intValue() > 15) {
                                    final Enrichment e = new Enrichment(score.intValue(), configuration.getName());
                                    enrichments.add(e);
                                }
                            }
                        }
                    } catch (CancellationException e) {
                        LOG.error("Task was cancelled " + task.getCommand());
                    }
                }
            }
            if( hitsToShow ){
                Collections.sort(enrichments);
                Collections.sort(sources);    
            }else{
                // maybe we can modify the query to broaden the search
                // replace all DefaultClause with an OrClause 
                //  [simply done with wrapping the query string inside ()'s ]
                // create and run a new RunningQueryImpl
                if( !queryStr.startsWith("(") && !queryStr.endsWith(")") ){
                    new RunningQueryImpl(context,'('+queryStr+')', parameters).run();
                }
            }
            
        } catch (Exception e) {
            LOG.error("Failure to run query.", e);
        }
    }

    private String getSingleParameter(final String paramName) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getSingleParameter()");
        } //</editor-fold>
        final String[] param = (String[]) parameters.get(paramName);

        if (param != null) {
            return param[0];
        } else {
            return null;
        }
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
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getNumberOfTerms()");
        } //</editor-fold>
        return QueryTokenizer.tokenize(queryStr).size();
    }


    public String getQueryString() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getQueryString()");
        } //</editor-fold>
        return queryStr;
    }

    public int getOffset() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getOffset(): " + offset);
        } //</editor-fold>
        return offset;
    }

    public void setOffset(final int offset) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("setOffset():" + offset);
        } //</editor-fold>
        this.offset = offset;
    }

    public Locale getLocale() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getLocale()");
        } //</editor-fold>
        return locale;
    }

    public SearchMode getSearchMode() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getSearchMode()");
        } //</editor-fold>
        return context.getSearchMode();
    }

    public List getSources() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getSources()");
        } //</editor-fold>
        return sources;
    }

    public void addSource(final Modifier modifier) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("addSource()");
        } //</editor-fold>
        sources.add(modifier);
    }

    public List getEnrichments() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getEnrichments()");
        } //</editor-fold>
        return enrichments;
    }

    public TokenEvaluatorFactory getTokenEvaluatorFactory() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getTokenEvaluatorFactory()");
        } //</editor-fold>
        return tokenEvaluatorFactory;
    }

    // TODO Find some other way to do this. Really do!
    public String getSourceParameters(final String source) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("getSourceParameters() Source=" + source);
        } //</editor-fold>

        if (source.equals("Norske nettsider")) {
            return "c=n";
        } else if (source.startsWith("Nyhets")) {
            return "c=m&amp;nav_sources=contentsourcenavigator";
        } else if (source.startsWith("Bild")) {
            return "c=p";
        } else if (source.startsWith("Person")) {
            return "c=w";
        } else if (source.startsWith("Bedrift")) {
            return "c=y";
        } else if (source.equals("Internasjonale nettsider")) {
            return "c=g";
        } else {
            return "c=d";
        }
    }

    public Query getQuery() {
        return queryObj;
    }



}
