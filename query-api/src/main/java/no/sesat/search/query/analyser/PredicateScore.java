/*
 * Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.analyser;

import org.apache.commons.collections.Predicate;

/**
 * PredicateScore provides a way to associate a {@link Predicate} with a score.
 *
 *
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
