/*
 * Copyright (2005-2009) Schibsted ASA
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


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.query.analyser.AnalysisRule;
import no.sesat.search.query.analyser.AnalysisRuleFactory;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.view.config.SearchTab.EnrichmentHint;
import org.apache.log4j.Logger;


/**
 * A RunningQuery implementing
 *      - Query Analysis,
 *      - Enrichments, and
 *      - RSS support.
 *
 * @version <tt>$Id$</tt>
 */
public class RunningQueryImpl extends AbstractRunningQuery implements RunningQuery {

   // Constants -----------------------------------------------------

    //FIXME: added since we had problems using the url-rewrite rules.
    public static final String PARAM_LAYOUT_OLD = "output";

    private static final Logger LOG = Logger.getLogger(RunningQueryImpl.class);
    private static final Logger ANALYSIS_LOG = Logger.getLogger("no.sesat.search.analyzer.Analysis");

    // Attributes ----------------------------------------------------

    private final AnalysisRuleFactory rules;

    /** */
    protected final DataModel datamodel;
    private final Map<String,Integer> scores = new HashMap<String,Integer>();
    private final Map<String,Integer> scoresByRule = new HashMap<String,Integer>();

    private final StringBuilder analysisReport;

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

        super(cxt, query);
        this.datamodel = cxt.getDataModel();

        LOG.trace("RunningQuery(cxt," + query + ')');

        rules = AnalysisRuleFactory.instanceOf(ContextWrapper.wrap(
                AnalysisRuleFactory.Context.class,
                context,
                new SiteContext(){
                    @Override
                    public Site getSite() {
                        return datamodel.getSite().getSite();
                    }
                },
                new BaseContext(){
                    public String getUniqueId(){
                        return datamodel.getParameters().getUniqueId();
                    }
            }));

        analysisReport = new StringBuilder(" <analyse><query>" + datamodel.getQuery().getXmlEscaped() + "</query>\n");
    }

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    @Override
    protected Collection<SearchCommand> buildCommands(){

        final Collection<SearchCommand> commands = super.buildCommands();
        ANALYSIS_LOG.info(analysisReport.toString() + " </analyse>");
        return commands;
    }

    @Override
    protected boolean addCommand(final SearchCommand.Context searchCmdCxt){

        boolean result = false;

        final SearchConfiguration conf = searchCmdCxt.getSearchConfiguration();
        final EnrichmentHint eHint = context.getSearchTab().getEnrichmentByCommand(conf.getId());
        if (eHint != null && !datamodel.getQuery().getQuery().isBlank()) {

            // search command marked as an enrichment
            if(useEnrichment(eHint, searchCmdCxt, analysisReport)){
                result = true;
            }
        }else{
            // normal search command
            result = super.addCommand(searchCmdCxt);
        }
        return result;
    }

    @Override
    protected boolean postProcessTask(
            final Future<ResultList<ResultItem>> task,
            final Map<Future<ResultList<ResultItem>>,SearchCommand> results)
            throws ExecutionException, InterruptedException {

        final ResultList<ResultItem> searchResult = task.get();
        // Information we need about and for the enrichment
        final SearchCommand command = results.get(task);
        final SearchConfiguration config = command.getSearchConfiguration();

        final String name = config.getId();
        final EnrichmentHint eHint = context.getSearchTab().getEnrichmentByCommand(name);

        final float score = scores.get(name) != null
                ? scores.get(name) * eHint.getWeight()
                : 0;

        // score
        if(eHint != null && searchResult.getHitCount() > 0 && score >= eHint.getThreshold()) {

            searchResult.addField(EnrichmentHint.NAME_KEY, name);
            searchResult.addObjectField(EnrichmentHint.SCORE_KEY, score);
            searchResult.addObjectField(EnrichmentHint.HINT_KEY, eHint);
            for(Map.Entry<String,String> property : eHint.getProperties().entrySet()){
                searchResult.addObjectField(property.getKey(), property.getValue());
            }
        }

        return super.postProcessTask(task, results);
    }

    /** Overridden to also include enrichment searches.
     *
     * @return collection of SearchConfigurations applicable to this running query.
     */
    @Override
    protected Collection<SearchConfiguration> applicableSearchConfigurations(){

        final Collection<SearchConfiguration> applicableSearchConfigurations = super.applicableSearchConfigurations();

        if(!isRss() && null == datamodel.getParameters().getValue(PARAM_COMMANDS)){
            for (SearchConfiguration conf : context.getSearchMode().getSearchConfigurations()) {

                // check for alwaysRun or for a possible enrichment (since its scoring will be the final indicator)
                boolean applicable = !conf.isAlwaysRun()
                        && (null != context.getSearchTab().getEnrichmentByCommand(conf.getId())
                        && !datamodel.getQuery().getQuery().isBlank());

                // add search configuration if applicable
                if(applicable){
                    applicableSearchConfigurations.add(conf);
                }
            }
        }

        return applicableSearchConfigurations;
    }

    // Private -------------------------------------------------------

    private boolean useEnrichment(
            final EnrichmentHint eHint,
            final SearchCommand.Context searchCmdCxt,
            final StringBuilder analysisReport){

        boolean result = false;

        final SearchConfiguration config = searchCmdCxt.getSearchConfiguration();
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
                                searchCmdCxt));

                    scoresByRule.put(eHint.getRule(), score);
                    analysisReport.append(analysisRuleReport);

                    LOG.debug("Score for " + config.getId() + " is " + score);

                } else {
                    score = scoresByRule.get(eHint.getRule());
                }
            }

            scores.put(config.getId(), score);

            result = score >= eHint.getThreshold();

        }

        return config.isAlwaysRun() || result;
    }

    private boolean isRss() {

        final StringDataObject outputParam = datamodel.getParameters().getValue(PARAM_LAYOUT);
        return null != outputParam && "rss".equals(outputParam.getString());
    }

    // Inner classes -------------------------------------------------
}
