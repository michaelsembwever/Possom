/*
 * Copyright (2005) Schibsted S�k AS
 *
 */
package no.schibstedsok.front.searchportal.query;

import edu.emory.mathcs.backport.java.util.concurrent.CancellationException;
import no.schibstedsok.front.searchportal.QueryTokenizer;
import no.schibstedsok.front.searchportal.analyzer.AnalysisRule;
import no.schibstedsok.front.searchportal.analyzer.AnalysisRules;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.impl.SearchCommandFactory;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import no.schibstedsok.front.searchportal.executor.SearchTask;
import no.schibstedsok.front.searchportal.i18n.TextMessages;
import no.schibstedsok.front.searchportal.result.Enrichment;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * An object representing a running query.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class RunningQuery {

    private static Log log = LogFactory.getLog(RunningQuery.class);
    private SearchMode searchMode = new SearchMode();
    private String query = "";
    private Map parameters;
    private int offset;
    private Locale locale;
    private List sources = new ArrayList();
    private TokenEvaluatorFactory tokenEvaluatorFactory;
    private List enrichments = new ArrayList();
    private Map hits = new HashMap();
    private static AnalysisRules rules = new AnalysisRules();
    private Map scores = new HashMap();


    /**
     * Create a new Running Query instance
     *
     * @param mode
     * @param query
     * @param parameters
     */
    public RunningQuery(SearchMode mode, String query, Map parameters) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: RunningQuery(): Params: " + parameters);
        }

        this.searchMode = mode;
        this.query = AdvancedQueryBuilder.trimDuplicateSpaces(query);

        if (query != null) {
            this.query = query.trim();
        }

        this.parameters = parameters;
        this.locale = new Locale("no", "NO");

        // This will among other things perform the initial fast search
        // for textual analysis.
        tokenEvaluatorFactory = new TokenEvaluatorFactoryImpl(query, XMLSearchTabsCreator.getInstance().getProperties());
    }

    /**
     * First find out if the user types in an advanced search etc by analyzing the query.
     * Then lookup correct tip using messageresources.
     * @return user tip
     */
    public String getGlobalSearchTips (){
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getGlobalSearchTips()");
        }
        if (AdvancedQueryBuilder.isAdvancedQuery(query)) {
            return TextMessages.getMessages().getMessage(locale,
                    "searchtip.use+-");
        } else {
            return null;
        }
        // return TextMessages.getMessages().getMessage("searchtip.use+-");
    }


    public Integer getNumberOfHits(String configName) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: getNumberOfHits()");
        }
        Integer i = (Integer)hits.get(configName);
        if(i == null){ i = new Integer(0); }
        return i;
    }

    /**
     * Thread run
     *
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        if(log.isDebugEnabled()){
            log.debug("ENTR: run()");
        }
        try {

            Collection commands = new ArrayList();

            for (Iterator iterator = searchMode.getSearchConfigurations().iterator(); iterator.hasNext();) {
                final SearchConfiguration searchConfiguration = (SearchConfiguration) iterator.next();

                // Factory responsible for creating commands against this configuration.
                //  This would normally be a final member variable and this class implement SearchCommandFactory.Context
                //   but it ain't a one-to-one query-to-configuration mapping ofcourse.
                final SearchCommandFactory cmdFactory = new SearchCommandFactory(new SearchCommandFactory.Context(){
                    public SearchConfiguration getSearchConfiguration(){
                        return searchConfiguration;
                    }
                });
                
                AnalysisRule rule = rules.getRule(searchConfiguration.getRule());

                if (rule != null) {
                    if (searchMode.getKey().equals("d") && offset == 0 ) {
                        if(log.isDebugEnabled()){
                            log.debug("run: searchMode.getKey().equals(d) && offset == 0");
                        }
                        int score = rule.evaluate(query, tokenEvaluatorFactory);

                        if (log.isDebugEnabled()) {
                            log.debug("Score for " + searchConfiguration.getName() + " is " + score);
                        }
                        scores.put(searchConfiguration.getName(), new Integer(score));

                        if (searchConfiguration.isAlwaysRunEnabled() || score >= searchConfiguration.getRuleThreshold()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Adding " + searchConfiguration.getName());
                            }
                            commands.add(cmdFactory.createSearchCommand(this, parameters));
                        }

                    } else if (searchConfiguration.isAlwaysRunEnabled()) {
                        commands.add(cmdFactory.createSearchCommand(this, parameters));
                    }
                } else {
                    // Optimazation. Alternate between the two web searches.
                    if (isNorwegian(searchConfiguration) || isInternational(searchConfiguration)) {
                        String searchType = getSingleParameter("s");
                        if (searchType != null && searchType.equals("g")) {
                            if (isInternational(searchConfiguration)) {
                                commands.add(cmdFactory.createSearchCommand(this, parameters));
                            }
                        } else if (isNorwegian(searchConfiguration)) {
                            commands.add(cmdFactory.createSearchCommand(this, parameters));
                        }
                    } else {
                        commands.add(cmdFactory.createSearchCommand(this, parameters));
                    }
                }
            }

            List results;

            if (log.isDebugEnabled()) {
                log.debug("run(): InvokeAll Commands.size=" + commands.size());
            }

            results = searchMode.getExecutor().invokeAll(commands, 3000);

            for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                SearchTask task = (SearchTask) iterator.next();

                SearchCommand command = task.getCommand();
                SearchConfiguration configuration = command.getSearchConfiguration();

                if (task.isDone()) {
                    try {
                        SearchResult searchResult = (SearchResult) task.get();

                        if (searchResult != null) {

                            hits.put(configuration.getName(), new Integer(searchResult.getHitCount()));

                            Integer score = (Integer) scores.get(task.getCommand().getSearchConfiguration().getName());

                            if (score != null && configuration.getRule() != null && score.intValue() >= task.getCommand().getSearchConfiguration().getRuleThreshold()) {
                                if (searchResult.getResults().size() > 0 && score.intValue() > 15) {
                                    Enrichment e = new Enrichment(score.intValue(), configuration.getName());
                                    enrichments.add(e);
                                }
                            }
                        }
                    } catch (CancellationException e) {
                        log.error("Task was cancelled " + task.getCommand());
                    }
                }
            }
            Collections.sort(enrichments);
            Collections.sort(sources);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private String getSingleParameter(String paramName) {
        if(log.isDebugEnabled()){
            log.debug("ENTR: getSingleParameter()");
        }
        String[] param = (String[]) parameters.get(paramName);

        if (param != null) {
            return param[0];
        } else {
            return null;
        }
    }

    private boolean isInternational(SearchConfiguration searchConfiguration) {
        return searchConfiguration.getName().equals("globalSearch");
    }

    private boolean isNorwegian(SearchConfiguration searchConfiguration) {
        return searchConfiguration.getName().equals("defaultSearch");
    }

    protected void addParameter(String key, Object obj) {
        parameters.put(key, obj);
    }

    public int getNumberOfTerms() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getNumberOfTerms()");
        }
        return QueryTokenizer.tokenize(query).size();
    }

    public String getQueryString() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getQueryString()");
        }
        return query;
    }

    public int getOffset() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getOffset(): " + offset);
        }
        return offset;
    }

    public void setOffset(int offset) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: setOffset():" + offset);
        }
        this.offset = offset;
    }

    public Locale getLocale() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getLocale()");
        }
        return locale;
    }

    public SearchMode getSearchMode() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getSearchMode()");
        }
        return searchMode;
    }

    public List getSources() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getSources()");
        }
        return sources;
    }

    public void addSource(Modifier modifier) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: addSource()");
        }
        sources.add(modifier);
    }

    public List getEnrichments() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getEnrichments()");
        }
        return enrichments;
    }

    public TokenEvaluatorFactory getTokenEvaluatorFactory() {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getTokenEvaluatorFactory()");
        }
        return tokenEvaluatorFactory;
    }

    // Find some other way to do this. Really do!
    public String getSourceParameters(String source) {
        if (log.isDebugEnabled()) {
            log.debug("ENTR: getSourceParameters() Source=" + source);
        }

        if (source.equals("Norske nettsider")) {
            return "c=n";
        } else if (source.startsWith("Nyhets")) {
            return "c=m&nav_sources=contentsourcenavigator";
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
}
