/*
 * Copyright (2005-2006) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.query.run;

import com.thoughtworks.xstream.XStream;
import edu.emory.mathcs.backport.java.util.concurrent.CancellationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.front.searchportal.QueryTokenizer;
import no.schibstedsok.front.searchportal.analyzer.AnalysisRule;
import no.schibstedsok.front.searchportal.analyzer.AnalysisRuleFactory;
import no.schibstedsok.front.searchportal.configuration.SearchTabsCreator;
import no.schibstedsok.front.searchportal.query.token.RegExpEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.ReportingTokenEvaluator;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.impl.SearchCommandFactory;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.executor.SearchTask;
import no.schibstedsok.front.searchportal.i18n.TextMessages;
import no.schibstedsok.front.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.parser.QueryParser;
import no.schibstedsok.front.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.front.searchportal.query.parser.ParseException;
import no.schibstedsok.front.searchportal.result.Enrichment;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.site.Site;
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
    private String strippedQueryString;

    private final Collection removers;


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


        queryStr = AdvancedQueryBuilder.trimDuplicateSpaces(query);
        removers = getStopWordRemovers();

        if (queryStr != null) {
            queryStr = queryStr.trim();
        }

        this.parameters = parameters;
        this.locale = new Locale("no", "NO");

        this.strippedQueryString = removeAllPrefixes(this.getQueryString());
        // This will among other things perform the initial fast search
        // for textual analysis.
        tokenEvaluatorFactory = new TokenEvaluatorFactoryImpl(
                new TokenEvaluatorFactoryImpl.Context() {
                    // <editor-fold defaultstate="collapsed" desc=" TokenEvaluatorFactoryImpl.Context ">
                    public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                        return context.newPropertiesLoader(resource, properties);
                    }

                    public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                        return context.newXStreamLoader(resource, xstream);
                    }

                    public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                        return context.newDocumentLoader(resource, builder);
                    }

                    public Site getSite() {
                        return context.getSite();
                    }

                    public String getQueryString()  {
                        return RunningQueryImpl.this.getQueryString();
                    }

                    public Properties getApplicationProperties() {
                        return XMLSearchTabsCreator.valueOf(new SearchTabsCreator.Context() {
                            // <editor-fold defaultstate="collapsed" desc=" SearchTabsCreator.Context ">
                            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                                return context.newPropertiesLoader(resource, properties);
                            }

                            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                                return context.newXStreamLoader(resource, xstream);
                            }

                            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                                return context.newDocumentLoader(resource, builder);
                            }

                            public Site getSite() {
                                return context.getSite();
                            }
                            //</editor-fold>
                        }).getProperties();
                    }
                    //</editor-fold>
                });

        // queryStr parser, avoid parsing an empty queryStr.
        if (queryStr != null && queryStr.length() > 0) {
            final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {

                public TokenEvaluatorFactory getTokenEvaluatorFactory() {
                    return tokenEvaluatorFactory;
                }
            });

            try  {
                queryObj = parser.getQuery();
            } catch (ParseException ex)  {
                LOG.error(ex);
            }
        }
        rules = AnalysisRuleFactory.valueOf(new AnalysisRuleFactory.Context() {
            // <editor-fold defaultstate="collapsed" desc=" AnalysisRuleFactory.Context ">
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return context.newPropertiesLoader(resource, properties);
            }

            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return context.newXStreamLoader(resource, xstream);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return context.newDocumentLoader(resource, builder);
            }

            public Site getSite() {
                return context.getSite();
            }
            //</editor-fold>
        });

    }

    private List getTokenMatches(final String token) {
        final ReportingTokenEvaluator e
                = (ReportingTokenEvaluator) tokenEvaluatorFactory.getEvaluator(TokenPredicate.valueOf(token));
        return e.reportToken(token, queryStr);
    }

    public List getGeographicMatches() {
        List matches = new ArrayList();

        matches.addAll(getTokenMatches("geolocal"));
        matches.addAll(getTokenMatches("geoglobal"));

        Collections.sort(matches);

        return matches;
    }

    private Collection getStopWordRemovers() {

        final TokenPredicate[] prefixes = {
            TokenPredicate.SITEPREFIX,
            TokenPredicate.CATALOGUEPREFIX,
            TokenPredicate.PICTUREPREFIX,
            TokenPredicate.NEWSPREFIX,
            TokenPredicate.WIKIPEDIAPREFIX,
            TokenPredicate.TVPREFIX,
            TokenPredicate.WEATHERPREFIX
        };
        Collection stopWordRemovers = new ArrayList();

        final RegExpEvaluatorFactory factory = RegExpEvaluatorFactory.valueOf(new RegExpEvaluatorFactory.Context() {
            // <editor-fold defaultstate="collapsed" desc=" RegExpEvaluatorFactory.Context ">
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return context.newPropertiesLoader(resource, properties);
            }

            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return context.newXStreamLoader(resource, xstream);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return context.newDocumentLoader(resource, builder);
            }

            public Site getSite() {
                return context.getSite();
            }
            //</editor-fold>
        });

        for (int i = 0; i < prefixes.length; i++) {
            final StopWordRemover remover = factory.getStopWordRemover(prefixes[i]);
            if (remover == null) {
                LOG.error("Failed to add " + prefixes[i]);
            }
            stopWordRemovers.add(remover);
        }
        return stopWordRemovers;
    }


    private String removeAllPrefixes(final String queryString) {
        String qStr = queryString;
        for (Iterator iter = removers.iterator(); iter.hasNext();) {
            final StopWordRemover remover = (StopWordRemover) iter.next();
            qStr = remover.removeStopWords(queryString);
        }

        return qStr.trim();
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
            LOG.trace("ENTR: getGlobalSearchTips()");
        } //</editor-fold>
        if (AdvancedQueryBuilder.isAdvancedQuery(queryStr)) {
            return TextMessages.valueOf(new TextMessages.Context() {
                // <editor-fold defaultstate="collapsed" desc=" TextMessages.Context ">
                public Site getSite() {
                    return context.getSite();
                }
                public PropertiesLoader newPropertiesLoader(final String rsc, final Properties props) {
                    return context.newPropertiesLoader(rsc, props);
                }
                // </editor-fold>
            }).getMessage("searchtip.use+-");
        } else {
            return null;
        }
    }


    public Integer getNumberOfHits(final String configName) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getNumberOfHits()");
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
            LOG.trace("ENTR: run()");
        } //</editor-fold>
        try {

            final Collection commands = new ArrayList();

            for (Iterator iterator = context.getSearchMode().getSearchConfigurations().iterator(); iterator.hasNext();) {
                final SearchConfiguration searchConfiguration = (SearchConfiguration) iterator.next();

                final SearchCommand.Context searchCmdCxt = new SearchCommand.Context() {
                    // <editor-fold defaultstate="collapsed" desc=" SearchCommand.Context ">
                    public SearchConfiguration getSearchConfiguration() {
                        return searchConfiguration;
                    }

                    public RunningQuery getRunningQuery() {
                        return RunningQueryImpl.this;
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
                    
                    public Query getQuery(){
                        return queryObj;
                    }
                    // </editor-fold>
                };

                final AnalysisRule rule = rules.getRule(searchConfiguration.getRule());

                if (rule != null) {

                    //if (context.getSearchMode().getKey().equals("d") && offset == 0) {
                    if (context.getSearchMode().isQueryAnalysisEnabled() && offset == 0) {

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("run: searchMode.getKey().equals(d) && offset == 0");
                        }

                        LOG.debug("Scoring old style for " + searchConfiguration.getRule());
                        final int oldScore = rule.evaluate(queryStr, tokenEvaluatorFactory);
                        LOG.debug("Scoring new style for " + searchConfiguration.getRule());
                        final int newScore = rule.evaluate(queryObj, tokenEvaluatorFactory);


                        assert (oldScore == newScore); // if this fails, goto mick, do not pass go, do not collect $200.
                        if (oldScore != newScore) {
                            LOG.fatal("\n\n!!! Old score does not match new score !!!\n");
                            LOG.fatal("OldScore: " + oldScore + "; NewScore: " + newScore + "; for " + searchConfiguration.getRule());
                        }

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Score for " + searchConfiguration.getName() + " is " + newScore);
                        }

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

            if (LOG.isDebugEnabled()) {
                LOG.debug("run(): InvokeAll Commands.size=" + commands.size());
            }

            final List results = context.getSearchMode().getExecutor().invokeAll(commands, 30000);

            // TODO This loop-(task.isDone()) code should become individual listeners to each executor to minimise time
            //  spent in task.isDone()
            for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                final SearchTask task = (SearchTask) iterator.next();

                final SearchCommand command = task.getCommand();
                final SearchConfiguration configuration = command.getSearchConfiguration();

                if (task.isDone()) {
                    try {
                        final SearchResult searchResult = (SearchResult) task.get();

                        if (searchResult != null) {

                            hits.put(configuration.getName(), new Integer(searchResult.getHitCount()));

                            final Integer score = (Integer) scores.get(task.getCommand().getSearchConfiguration().getName());

                            if (score != null && configuration.getRule() != null && score.intValue() >= task.getCommand().getSearchConfiguration().getRuleThreshold()) {
                                if (searchResult.getResults().size() > 0 && score.intValue() > 15) {
                                    Enrichment e = new Enrichment(score.intValue(), configuration.getName());
                                    enrichments.add(e);
                                }
                            }
                        }
                    } catch (CancellationException e) {
                        LOG.error("Task was cancelled " + task.getCommand());
                    }
                }
            }
            Collections.sort(enrichments);
            Collections.sort(sources);
        } catch (Exception e) {
            LOG.error("Failure to run query.", e);
        }
    }

    private String getSingleParameter(final String paramName) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getSingleParameter()");
        } //</editor-fold>
        String[] param = (String[]) parameters.get(paramName);

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
            LOG.trace("ENTR: getNumberOfTerms()");
        } //</editor-fold>
        return QueryTokenizer.tokenize(queryStr).size();
    }


    public String getQueryString() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getQueryString()");
        } //</editor-fold>
        return queryStr;
    }

    public int getOffset() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getOffset(): " + offset);
        } //</editor-fold>
        return offset;
    }

    public void setOffset(final int offset) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: setOffset():" + offset);
        } //</editor-fold>
        this.offset = offset;
    }

    public Locale getLocale() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getLocale()");
        } //</editor-fold>
        return locale;
    }

    public SearchMode getSearchMode() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getSearchMode()");
        } //</editor-fold>
        return context.getSearchMode();
    }

    public List getSources() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getSources()");
        } //</editor-fold>
        return sources;
    }

    public void addSource(final Modifier modifier) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: addSource()");
        } //</editor-fold>
        sources.add(modifier);
    }

    public List getEnrichments() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getEnrichments()");
        } //</editor-fold>
        return enrichments;
    }

    public TokenEvaluatorFactory getTokenEvaluatorFactory() {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getTokenEvaluatorFactory()");
        } //</editor-fold>
        return tokenEvaluatorFactory;
    }

    // TODO Find some other way to do this. Really do!
    public String getSourceParameters(final String source) {
        // <editor-fold defaultstate="collapsed" desc=" Trace ">
        if (LOG.isTraceEnabled()) {
            LOG.trace("ENTR: getSourceParameters() Source=" + source);
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



    /**
     * Get the strippedQueryString.
     *
     * @return the strippedQueryString.
     */
    public String getStrippedQueryString() {
        return strippedQueryString;
    }

    public Query getQuery() {
        return queryObj;
    }



}
