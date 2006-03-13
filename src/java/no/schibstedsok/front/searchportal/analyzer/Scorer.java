/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Scorer.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.schibstedsok.front.searchportal.analyzer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

/** Responsible for Visiting the Query and scoring a total according
 *   to the rule's predicateScores listed in the context.
 * This class is not thread-safe.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class Scorer extends AbstractReflectionVisitor {

    /** The contextual dependencies the Scorer requires to calculate a total score for this Query it will visit. */
    public interface Context {

        /** The rules list of PredicateScores. In a map with mappings of the PredicateScore to Predicate.
         * This mapping is the same as predicateScore.getPredicate() but exists so give the possibility of
         * set manipulation.
         * @return map of predicateScores to Predicates for all PredicateScores in the rule we are scoring for.
         **/
        Map/*<PredicateScore,Predicate>*/ getPredicates();

        /** The TokenEvaluatorFactory we will use to obtain evaluators for each Predicate.
         * @return the tokenEvaluatorFactory.
         **/
        TokenEvaluatorFactory getTokenEvaluatorFactory();

        String getNameForAnonymousPredicate(Predicate predicate);

    }

    private static final Logger LOG = Logger.getLogger(Scorer.class);
    private static final Logger ANALYSIS_LOG = Logger.getLogger("no.schibstedsok.front.searchportal.analyzer.Analysis");

    private int score = 0;
    private boolean additivity = true;
    private Context context;
    private Set/*<Predicate>*/ touchedPredicates = new HashSet/*<Predicate>*/();

    private static final String DEBUG_UPDATE_SCORE = "Updating Score...";

    /** Create the Scorer with the required context.
     * @param cxt the context this must work against.
     **/
    public Scorer(final Context cxt) {
        context = cxt;
    }

    /** the scoring result. should not be called before visiting is over.
     **/
    public int getScore() {
        return score;
    }

    protected void visitImpl(final AndClause clause) {
        final boolean originalAdditivity = additivity;
        additivity = true;
        clause.getFirstClause().accept(this);
        scoreClause(clause);
        clause.getSecondClause().accept(this);
        additivity = originalAdditivity;
    }

    protected void visitImpl(final OrClause clause) {
        final boolean originalAdditivity = additivity;
        additivity = true;
        clause.getFirstClause().accept(this);
        scoreClause(clause);
        clause.getSecondClause().accept(this);
        additivity = originalAdditivity;
    }

    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        scoreClause(clause);
        clause.getSecondClause().accept(this);
    }

    protected void visitImpl(final NotClause clause) {
        final boolean originalAdditivity = additivity;
        additivity = false;
        clause.getFirstClause().accept(this);
        scoreClause(clause);
        additivity = originalAdditivity;
    }

    protected void visitImpl(final AndNotClause clause) {
        final boolean originalAdditivity = additivity;
        additivity = false;
        scoreClause(clause);
        clause.getFirstClause().accept(this);
        additivity = originalAdditivity;
    }

    protected void visitImpl(final Clause clause) {
        scoreClause(clause);
    }

    /** Find if this clause contains (either known, possible, or custom joined) predicates correspondng to
     * PredicateScores listed in the context.
     * Avoid scoring predicates already matched.
     * @param the clause we are scoring.
     * @param addition whether the score will be added or subtracted.
     */
    private void scoreClause(final Clause clause) {
        final Set/*<Predicate>*/ knownPredicates = clause.getKnownPredicates();
        final Set/*<Predicate>*/ possiblePredicates = clause.getPossiblePredicates();

        // XXX Couldn't find the set algorythm for joining two set in Core Java or Commons Collections :-/
        final Iterator/*<PredicateScore>*/ it = context.getPredicates().keySet().iterator();
        while (it.hasNext()) {

            final PredicateScore predicateScore = (PredicateScore) it.next();
            final Predicate predicate = predicateScore.getPredicate();

            // check we haven't already scored with this predicate.
            if (!touchedPredicates.contains(predicate)) {

                // update the factory with the predicate sets that can be used to improve evaluation performance.
                final TokenEvaluatorFactory factory = context.getTokenEvaluatorFactory();
                factory.setClausesKnownPredicates(clause.getKnownPredicates());
                factory.setClausesPossiblePredicates(clause.getPossiblePredicates());

                if (knownPredicates.contains(predicate)
                        // OR if this is a possiblePredicate or a all|any|none|not predicate
                        //  find out if it is now applicable...
                        || ((possiblePredicates.contains(predicate) || !(predicate instanceof TokenPredicate))
                                && predicate.evaluate(factory))) {

                    if (additivity) {
                        addScore(predicateScore);
                    }  else  {
                        minusScore(predicateScore);
                    }
                }
            }
        }
    }


    private void addScore(final PredicateScore predicateScore) {

        final Predicate predicate = predicateScore.getPredicate();
        score += predicateScore.getScore();
        touchedPredicates.add(predicate);

        LOG.debug(DEBUG_UPDATE_SCORE + predicate + " adds " + predicateScore.getScore());

        ANALYSIS_LOG.info("  <predicate-add name=\""
                + (predicate instanceof TokenPredicate
                    ? predicate.toString()
                    : context.getNameForAnonymousPredicate(predicate))
                + "\">"+predicateScore.getScore()
                + "</predicate>");
    }

    private void minusScore(final PredicateScore predicateScore) {

        final Predicate predicate = predicateScore.getPredicate();
        score -= predicateScore.getScore();
        touchedPredicates.add(predicate);

        LOG.debug(DEBUG_UPDATE_SCORE + predicate + " minus " + predicateScore.getScore());

        ANALYSIS_LOG.info("  <predicate-minus name=\""
                + (predicate instanceof TokenPredicate
                    ? predicate.toString()
                    : context.getNameForAnonymousPredicate(predicate))
                + "\">"
                + predicateScore.getScore()
                + "</predicate>");

    }
}
