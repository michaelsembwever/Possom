/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.analyser;


import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineContext;
import no.schibstedsok.searchportal.query.token.TokenPredicate;
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
    
    public interface Context extends TokenEvaluationEngineContext{
        Appendable getReportBuffer();
    }

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
     * Evaluates this rule. All added predicates are evaluated using engine
     * as input. The score of those predicates that are true are added to the
     * final score (output of this method).
     *
     * @param query
     *            the query to apply the rule to.
     * @param engine
     *            the {@link no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl} used as input to the
     *            predicates.
     * @return the score of this rule when applied to query.
     */
    public int evaluate(final Query query, final Context context) {

        final boolean additivity = true; // TODO implement inside NOT ANDNOT clauses to deduct from score.

        final Scorer scorer = new Scorer(ContextWrapper.wrap(Scorer.Context.class, 
                new BaseContext() {
                    public String getNameForAnonymousPredicate(final Predicate predicate) {
                        return predicateNames.get(predicate);
                    }
                },
                context));

        try{
            // update the engine with the query's evaluation state
            context.getTokenEvaluationEngine().setState(query.getEvaluationState());

            for (PredicateScore predicateScore : predicates.keySet()) {
                try{
                    
                    assert null != predicateScore.getPredicate() 
                            : "Disappearing predicate from score " + predicateScore;
                    
                    if (predicateScore.getPredicate().evaluate(context.getTokenEvaluationEngine())) {

                        if (additivity) {
                            scorer.addScore(predicateScore);
                        }  else  {
                            scorer.minusScore(predicateScore);
                        }
                    }

                }catch(TokenPredicate.EvaluationException ie){
                    // make sure to mention in the analysis logs that the scoring is corrupt.
                    scorer.error(predicateScore);
                }
            }
        }finally{
            context.getTokenEvaluationEngine().setState(null);
        }

        return scorer.getScore();
    }

    /** TODO comment me. **/
    void setPredicateNameMap(final Map<Predicate,String> predicateNames) {
        this.predicateNames = predicateNames;
    }

}
