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
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.front.searchportal.query.parser.AndClause;
import no.schibstedsok.front.searchportal.query.parser.AndNotClause;
import no.schibstedsok.front.searchportal.query.parser.Clause;
import no.schibstedsok.front.searchportal.query.parser.NotClause;
import no.schibstedsok.front.searchportal.query.parser.OrClause;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class Scorer extends AbstractReflectionVisitor {

    public interface Context {

        Map/*<PredicateScore,Predicate>*/ getPredicates();

        TokenEvaluatorFactory getTokenEvaluatorFactory();

    }

    private static final Log LOG = LogFactory.getLog(Scorer.class);

    private int score = 0;
    private Context context;
    private Set/*<Predicate>*/ touchedPredicates = new HashSet/*<Predicate>*/();

    private static final String DEBUG_UPDATE_SCORE = "Updating Score...";

    public Scorer(final Context cxt) {
        context = cxt;
    }

    public int getScore() {
        return score;
    }

    public void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        visitImpl((Clause) clause);
        clause.getSecondClause().accept(this);
    }

    public void visitImpl(final OrClause clause) {
        clause.getFirstClause().accept(this);
        visitImpl((Clause) clause);
        clause.getSecondClause().accept(this);
    }

    public void visitImpl(final NotClause clause) {
        clause.getClause().accept(this);
        visitImpl((Clause) clause); // FIXME do i need to reverse this score ??

    }

    public void visitImpl(final AndNotClause clause) {
        clause.getFirstClause().accept(this);
        visitImpl((Clause) clause); // FIXME do i need to reverse this score ??
        clause.getSecondClause().accept(this);
    }

    public void visitImpl(final Clause clause) {
        final Set/*<Predicate>*/ knownPredicates = clause.getKnownPredicates();
        final Set/*<Predicate>*/ possiblePredicates = clause.getPossiblePredicates();

        // XXX Couldn't find the set algorythm for joining two set in Core Java or Commons Collections :-/
        final Iterator/*<PredicateScore>*/ it = context.getPredicates().keySet().iterator();
        while ( it.hasNext() ) {

            final PredicateScore predicateScore = (PredicateScore) it.next();
            final Predicate predicate = predicateScore.getPredicate();

            // check we haven't already scored with this predicate.
            if ( !touchedPredicates.contains(predicate) ) {
                if ( knownPredicates.contains(predicate) )  {
                    addScore(predicateScore);
                }
                if ( possiblePredicates.contains(predicate) || !(predicate instanceof TokenPredicate) ) {
                    // if this is a possiblePredicate or a and|or|none|any predicate
                    //  find out if it is now applicable...
                    if ( predicate.evaluate(context.getTokenEvaluatorFactory()) ) {
                        addScore(predicateScore);
                    }
                }
            }
        }
    }

    private void addScore(final PredicateScore predicateScore) {
        this.score += predicateScore.getScore();
        touchedPredicates.add(predicateScore.getPredicate());
        LOG.debug(DEBUG_UPDATE_SCORE + predicateScore.getPredicate() + " adds " + predicateScore.getScore());
    }
}
