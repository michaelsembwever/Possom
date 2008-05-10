/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Scorer.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.sesat.search.query.analyser;

import java.io.IOException;
import no.sesat.search.query.parser.AbstractReflectionVisitor;
import no.sesat.search.query.token.TokenPredicate;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

/** Responsible for Visiting the Query and scoring a total according
 *   to the rule's predicateScores listed in the context.
 * This class is not thread-safe.
 *
 * @version $Id$
 *
 */
public final class Scorer extends AbstractReflectionVisitor {

    /** The contextual dependencies the Scorer requires to calculate a total score for this Query it will visit. */
    public interface Context extends AnalysisRule.Context {

        /** TODO comment me. **/
        String getNameForAnonymousPredicate(Predicate predicate);
    }

    private static final Logger LOG = Logger.getLogger(Scorer.class);

    private int score = 0;
    private final Context context;

    private static final String DEBUG_UPDATE_SCORE = "Updating Score...";
    private static final String INFO_STALE_SCORE = "Scoring failed...";

    /** Create the Scorer with the required context.
     **/
    public Scorer(final Context cxt) {
        context = cxt;
    }

    /** the scoring result. should not be called before visiting is over.
     **/
    public int getScore() {
        return score;
    }

    /** TODO comment me. **/
    public void addScore(final PredicateScore predicateScore) {

        final Predicate predicate = predicateScore.getPredicate();

        LOG.debug(DEBUG_UPDATE_SCORE + toString(predicate) + " adds " + predicateScore.getScore());

        report("   <predicate-add name=\"" + toString(predicate) + "\">"
                + predicateScore.getScore()
                + "</predicate-add>\n");

        score += predicateScore.getScore();
    }

    /** TODO comment me. **/
    public void minusScore(final PredicateScore predicateScore) {

        final Predicate predicate = predicateScore.getPredicate();

        LOG.debug(DEBUG_UPDATE_SCORE + toString(predicate) + " subtracts " + predicateScore.getScore());

        report("   <predicate-minus name=\"" + toString(predicate) + "\">"
                + predicateScore.getScore()
                + "</predicate-minus>\n");

        score -= predicateScore.getScore();
    }

    /** TODO comment me. **/
    public void error(final PredicateScore predicateScore) {

        final Predicate predicate = predicateScore.getPredicate();

        LOG.info(INFO_STALE_SCORE + toString(predicate));

        report("   <predicate-error name=\"" + toString(predicate) + "\">±"
                + predicateScore.getScore()
                + "</predicate-error>\n");
    }

    private void report(final String msg){

        try{
            context.getReportBuffer().append(msg);

        }catch(IOException ioe){
            LOG.warn(ioe.getMessage());
        }
    }

    private String toString(final Predicate predicate){

        return predicate instanceof TokenPredicate
                    ? predicate.toString()
                    : context.getNameForAnonymousPredicate(predicate);
    }
}
