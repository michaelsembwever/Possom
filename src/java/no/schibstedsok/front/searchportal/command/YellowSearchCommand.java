/*
 * Copyright (2005) Schibsted S¿k AS
 */
package no.schibstedsok.front.searchportal.command;

import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import no.fast.ds.search.BaseParameter;
import no.fast.ds.search.SearchParameter;
import no.schibstedsok.front.searchportal.analyzer.AnalysisRule;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenPredicate;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.query.RunningQuery;

/**
 * A YellowSearchCommand.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class YellowSearchCommand extends FastSearchCommand {

    private static final String YELLOWPAGES3_YPNAVN2 = "+yellowpages3 +ypnavn2";

    private static final int MATCH_SCORE = 10;

    /**
     * Create a new YellowSearchCommand.
     *
     * @param query
     *            the current query.
     * @param config
     *            the configuration for this command.
     * @param parameters
     *            parameters.
     */
    public YellowSearchCommand(final RunningQuery query,
            final FastConfiguration config, final Map parameters) {
        super(query, config, parameters);
    }

    /**
     * Creates a search parameter based on query analysis. If the query matches
     * either a company or a priority company the
     * <code>YELLOWPAGES3_YPNAVN2</code> sorting parameter is chosen. Otherwise
     * the default (as specified by the search configuration) sort order is
     * chosen.
     *
     * @return a SearchParameter.
     */
    protected SearchParameter getSortByParameter() {

        Predicate company = new TokenPredicate("exact_company");
        Predicate prioCompany = new TokenPredicate("exact_companypriority");

        Predicate eitherCompany = PredicateUtils.orPredicate(company, prioCompany);

        AnalysisRule rule = new AnalysisRule();
        rule.addPredicateScore(eitherCompany, MATCH_SCORE);

        TokenEvaluatorFactory factory = getQuery().getTokenEvaluatorFactory();

        int score = rule.evaluate(getQuery().getQueryString(), factory);

        if (score > 0) {
            return new SearchParameter(BaseParameter.SORT_BY,
                    YELLOWPAGES3_YPNAVN2);
        } else {
            if (fastConfiguration.getSortBy() != null) {
                return new SearchParameter(BaseParameter.SORT_BY,
                        fastConfiguration.getSortBy());
            } else {
                return null;
            }
        }
    }
}
