/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.analyser;


import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;


/**
 * The AnalysisRule provides scoring of a query based on a set of
 * {@link Predicate} instances.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public final class AnalysisRule {

    private static final Logger LOG = Logger.getLogger(AnalysisRule.class);

    /** Although we have access to the Predicates through the PredicateScore object it is possible to do set arithmetic
     * when we can access the predicate collection wihtout looping them out first.
     **/
    private final Map<PredicateScore,Predicate> predicates = new HashMap<PredicateScore,Predicate>();

    private Map<Predicate,String> predicateNames;


    /**
     * Adds a {@link Predicate} and an accompanying score. The predicate will at
     * evaluation time be evaluated with a {@link TokenEvaluationEngine} as
     * input.
     *
     * @param predicate
     *            a predicate to evaluate at evaluation time.
     * @param score
     *            the score associated with the predicate.
     */
    public void addPredicateScore(final Predicate predicate, final int score) {
        final PredicateScore pScore = new PredicateScore(predicate, score);
        predicates.put(pScore, predicate);
    }

    /**
     * Evaluates this rule. All added predicates are evaluated using evalFactory
     * as input. The score of those predicates that are true are added to the
     * final score (output of this method).
     *
     *
     * @param query
     *            the query to apply the rule to.
     * @param evalFactory
     *            the {@link TokenEvaluationEngineImpl} used as input to the
     *            predicates.
     * @return the score of this rule when applied to query.
     */
    public int evaluate(final Query query, final TokenEvaluationEngine evalFactory) {
        int score = 0;

        // we're done with parsing individual terms.
        //  we need to do this to ensure possible predicates are now checked against the whole query string.
        evalFactory.setCurrentTerm(null);

        // New (post-QueryParser) implementation.
        final Scorer scorer = new Scorer(new Scorer.Context() {
            public TokenEvaluationEngine getTokenEvaluationEngine() {
                return evalFactory;
            }

            public Map<PredicateScore,Predicate> getPredicates() {
                return predicates;
            }

            public String getNameForAnonymousPredicate(final Predicate predicate) {
                return predicateNames.get(predicate);
            }

        });

        if (query != null) {
            scorer.visit(query.getRootClause());
            score = scorer.getScore();
        }

        return score;
    }

    /** TODO comment me. **/
    void setPredicateNameMap(final Map<Predicate,String> predicateNames) {
        this.predicateNames = predicateNames;
    }
}
