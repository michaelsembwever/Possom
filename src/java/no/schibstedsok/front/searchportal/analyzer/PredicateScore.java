package no.schibstedsok.front.searchportal.analyzer;

import org.apache.commons.collections.Predicate;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PredicateScore {

    private Predicate predicate;
    private int score;

    public int getScore() {
        return score;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public PredicateScore(Predicate p, int score) {
        this.predicate = p;
        this.score = score;
    }
}
