/* Copyright (2005-2006) Schibsted Søk AS
 *
 * Scorer.java
 *
 * Created on 13 January 2006, 09:58
 *
 */

package no.schibstedsok.front.searchportal.analyzer;

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
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class Scorer extends AbstractReflectionVisitor {
    
    public interface Context{
        
        abstract Map/*<PredicateScore,Predicate>*/ getPredicates();

        abstract TokenEvaluatorFactory getTokenEvaluatorFactory();

    }
    
    private int score = 0;
    private Context context;
    
    public Scorer(final Context cxt){
        context = cxt;
    }

    public int getScore() {
        return score;
    }

    public void visitImpl(AndClause clause) {
        clause.getFirstClause().accept(this);
        visitImpl((Clause) clause);
        clause.getSecondClause().accept(this);
    }

    public void visitImpl(OrClause clause) {
        clause.getFirstClause().accept(this);
        visitImpl((Clause) clause);
        clause.getSecondClause().accept(this);
    }

    public void visitImpl(NotClause clause) {
        clause.getClause().accept(this);
        visitImpl((Clause) clause); // FIXME do i need to reverse this score ??

    }

    public void visitImpl(AndNotClause clause) {
        clause.getFirstClause().accept(this);
        visitImpl((Clause) clause); // FIXME do i need to reverse this score ??
        clause.getSecondClause().accept(this);
    }

    public void visitImpl(Clause clause) {
        final Set/*<Predicate>*/ knownPredicates = clause.getKnownPredicates();
        final Set/*<Predicate>*/ possiblePredicates = clause.getPossiblePredicates();

        // XXX Couldn't find the set algorythm for joining two set in Core Java or Commons Collections :-/
        final Iterator/*<PredicateScore>*/ it = context.getPredicates().keySet().iterator();
        while ( it.hasNext() ) {
            final PredicateScore predicateScore = (PredicateScore) it.next();
            final Predicate predicate = predicateScore.getPredicate();
            if ( knownPredicates.contains(predicate) ) {
                score += predicateScore.getScore();
            }
            if ( possiblePredicates.contains(predicate) ) {
                // currently applicable?
                if ( predicate.evaluate(context.getTokenEvaluatorFactory()) ) {
                    score += predicateScore.getScore();
                }
            }
        }
    }
}
