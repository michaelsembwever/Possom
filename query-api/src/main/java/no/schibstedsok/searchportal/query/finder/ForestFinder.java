// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.*;

import org.apache.log4j.Logger;

public final class ForestFinder extends AbstractReflectionVisitor {


    private static final Logger LOG = Logger.getLogger(ForestFinder.class);
    private static final String DEBUG_COUNT_TO = " trees in forest ";
    private boolean searching = false;
    private final List<DoubleOperatorClause> roots = new ArrayList<DoubleOperatorClause>();

    private static final String ERR_CANNOT_CALL_VISIT_DIRECTLY 
            = "visit(object) can't be called directly on this visitor!";

    public synchronized List<DoubleOperatorClause> findForestRoots(final OperationClause root) {

        if (searching) {
            throw new IllegalStateException(ERR_CANNOT_CALL_VISIT_DIRECTLY);
        }
        searching = true;
        roots.clear();
        visit(root);
        searching = false;
        return Collections.unmodifiableList(roots);
    }


    protected void visitImpl(final OperationClause clause) {

        clause.getFirstClause().accept(this);
    }

    protected void visitImpl(final XorClause clause) {

        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    protected void visitImpl(final DoubleOperatorClause clause) {

        final DoubleOperatorClause forestDepth = forestWalk(clause);
        clause.getFirstClause().accept(this);
        forestDepth.getSecondClause().accept(this);
    }

    protected void visitImpl(final LeafClause clause) {
        // leaves can't be forest roots :-)
    }

    /** Returns the deepest tree in the forest.
     * And adds the forest to the roots if it contains more than one tree.
     **/
    private <T extends DoubleOperatorClause> T forestWalk(final T clause){

        int count = 1;
        T forestDepth = clause;
        // presumption below is that forests can't mix implementation classes, not just interfaces.
        for (; forestDepth.getSecondClause().getClass() == clause.getClass(); forestDepth = (T) forestDepth.getSecondClause()){
            ++count;
        }
        LOG.debug(count + DEBUG_COUNT_TO + clause);
        if(count >1){
            roots.add(clause);
        }
        return forestDepth;
    }

}