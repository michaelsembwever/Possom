/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.query.analyser;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.query.Query;
import no.sesat.search.query.token.TokenEvaluationEngineContext;
import no.sesat.search.query.token.TokenPredicate;
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
        String getRuleName();
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
     *            the {@link no.sesat.search.query.token.TokenEvaluationEngineImpl} used as input to the
     *            predicates.
     * @return the score of this rule when applied to query.
     */
    public int evaluate(final Query query, final Context context) {

        final boolean additivity = true; // TODO implement inside NOT ANDNOT clauses to deduct from score.
        
        final StringBuilder internalReport = new StringBuilder();

        final Scorer scorer = new Scorer(ContextWrapper.wrap(Scorer.Context.class, 
                new BaseContext() {
                    public String getNameForAnonymousPredicate(final Predicate predicate) {
                        return predicateNames.get(predicate);
                    }
                    public Appendable getReportBuffer(){
                        return internalReport;
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
            
            context.getReportBuffer().append(
                    "  <analysis name=\"" + context.getRuleName() + "\" score=\"" + scorer.getScore() + "\">\n"
                    +     internalReport.toString()
                    + "  </analysis>\n");
            
        }catch(IOException ioe){
            LOG.warn("Failed to append report results", ioe);
            
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
