/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.query.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.*;

import org.apache.log4j.Logger;

/**
 * 
 * @author mick
 * @version $Id$
 */
public final class ForestFinder extends AbstractReflectionVisitor {


    private static final Logger LOG = Logger.getLogger(ForestFinder.class);
    private static final String DEBUG_COUNT_TO = " trees in forest ";
    private boolean searching = false;
    private final List<DoubleOperatorClause> roots = new ArrayList<DoubleOperatorClause>();

    private static final String ERR_CANNOT_CALL_VISIT_DIRECTLY 
            = "visit(object) can't be called directly on this visitor!";

    /**
     * 
     * @param root 
     * @return 
     */
    public synchronized List<DoubleOperatorClause> findForestRoots(final OperationClause root) {

        if (searching) {
            throw new IllegalStateException(ERR_CANNOT_CALL_VISIT_DIRECTLY);
        }
        searching = true;
        roots.clear();
        visit(root);
        searching = false;
        return Collections.unmodifiableList(new ArrayList<DoubleOperatorClause>(roots));
    }


    /**
     * 
     * @param clause 
     */
    protected void visitImpl(final OperationClause clause) {

        clause.getFirstClause().accept(this);
    }

    /**
     * 
     * @param clause 
     */
    protected void visitImpl(final XorClause clause) {

        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * 
     * @param clause 
     */
    protected void visitImpl(final DoubleOperatorClause clause) {

        final DoubleOperatorClause forestDepth = forestWalk(clause);
        clause.getFirstClause().accept(this);
        forestDepth.getSecondClause().accept(this);
    }

    /**
     * 
     * @param clause 
     */
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