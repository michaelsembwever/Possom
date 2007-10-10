/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

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