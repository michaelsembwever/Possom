package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.collections.Predicate;

import java.util.*;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class AnalysisRule {

    Log log = LogFactory.getLog(AnalysisRule.class);

    public Collection predicates = new ArrayList();

    public void addPredicateScore(Predicate p, int score) {
        PredicateScore pScore = new PredicateScore(p, score);
        predicates.add(pScore);
    }

    public int analyze(String query, TokenEvaluatorFactory evalFactory) {
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
