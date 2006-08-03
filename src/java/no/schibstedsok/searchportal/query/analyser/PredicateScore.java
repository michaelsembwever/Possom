/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.analyser;

import org.apache.commons.collections.Predicate;

/**
 * PredicateScore provides a way to associate a {@link Predicate} with a score.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class PredicateScore {

    private Predicate predicate;
    private int score;

    /**
     * Create a new PredicateScore. Associating <code>p</code> with
     * <code>score</code>
     *
     * @param p     the predicate.
     * @param score the score.
     */
    public PredicateScore(final Predicate p, final int score) {
        this.predicate = p;
        this.score = score;
    }

    /**
     * Returns the predicate.
     *
     * @return  the predicate.
     */
    public Predicate getPredicate() {
        return predicate;
    }

    /**
     * Returns the score.
     *
     * @return  the score.
     */
    public int getScore() {
        return score;
    }
}
