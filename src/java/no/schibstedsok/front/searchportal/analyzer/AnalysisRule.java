/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.analyzer;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.front.searchportal.query.parser.AndClause;
import no.schibstedsok.front.searchportal.query.parser.AndNotClause;
import no.schibstedsok.front.searchportal.query.parser.Clause;
import no.schibstedsok.front.searchportal.query.parser.NotClause;
import no.schibstedsok.front.searchportal.query.parser.OrClause;
import no.schibstedsok.front.searchportal.query.parser.Query;

import org.apache.commons.collections.Predicate;

/**
 * The AnalysisRule provides scoring of a query based on a set of
 * {@link Predicate} instances.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public final class AnalysisRule {

    /** Although we have access to the Predicates through the PredicateScore object it is possible to do set arithmetic
     * when we can access the predicate collection wihtout looping them out first.
     **/
    private final Map/*<PredicateScore,Predicate>*/ predicates = new HashMap/*<PredicateScore,Predicate>*/();

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
     *            the {@link TokenEvaluatorFactoryImpl} used as input to the
     *            predicates.
     * @return the score of this rule when applied to query.
     */
    public int evaluate(final String query, final TokenEvaluatorFactory evalFactory) {
        int score = 0;

        // Old (pre-QueryParser) implementation.
        for (Iterator iterator = predicates.keySet().iterator(); iterator.hasNext();) {
            final PredicateScore p = (PredicateScore) iterator.next();

            final boolean match = p.getPredicate().evaluate(evalFactory);

            if (match) {
                score += p.getScore();
            }
        }

        return score;
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
     *            the {@link TokenEvaluatorFactoryImpl} used as input to the
     *            predicates.
     * @return the score of this rule when applied to query.
     */
    public int evaluate(final Query query, final TokenEvaluatorFactory evalFactory) {
        int score = 0;

        // we're done with parsing individual terms.
        evalFactory.setCurrentTerm(null);

        // New (post-QueryParser) implementation.
        final Scorer scorer = new Scorer(new Scorer.Context() {
            public TokenEvaluatorFactory getTokenEvaluatorFactory() {
                return evalFactory;
            }
            
            public Map/*<PredicateScore,Predicate>*/ getPredicates(){
                return predicates;
            }

        });
        scorer.visit(query.getRootClause());

        return scorer.getScore();
    }

}
