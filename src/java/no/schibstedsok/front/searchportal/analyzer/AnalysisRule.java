/*
 * Copyright (2005) Schibsted S¿k AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.Predicate;

/**
 * The AnalysisRule provides scoring of a query based on a set of
 * {@link Predicate} instances.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class AnalysisRule {

    private Collection predicates = new ArrayList();

    /**
     * Adds a {@link Predicate} and an accompanying score. The predicate will at
     * evaluation time be evaluated with a {@link TokenEvaluatorFactory} as
     * input.
     *
     * @param predicate
     *            a predicate to evaluate at evaluation time.
     * @param score
     *            the score associated with the predicate.
     */
    public void addPredicateScore(final Predicate predicate, final int score) {
        PredicateScore pScore = new PredicateScore(predicate, score);
        predicates.add(pScore);
    }

    /**
     * Evaluates this rule. All added predicates are evaluated using evalFactory
     * as input. The score of those predicates that are true are added to the
     * final score (output of this method).
     *
     * @param query
     *            the query to apply the rule to.
     * @param evalFactory
     *            the {@link TokenEvaluatorFactoryImpl} used as input to the
     *            predicates.
     * @return the score of this rule when applied to query.
     */
    public int evaluate(final String query, final TokenEvaluatorFactory evalFactory) {
        int score = 0;

        for (Iterator iterator = predicates.iterator(); iterator.hasNext();) {
            PredicateScore p = (PredicateScore) iterator.next();

            boolean match = p.getPredicate().evaluate(evalFactory);

            if (match) {
                score += p.getScore();
            }
        }
        return score;
    }
}
