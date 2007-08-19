/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.query.finder;

import java.io.Serializable;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.*;



/** Simple visitor used to count the number of leaves under a given clause.
 * 
 * @author mick
 * @version $Id$
 */
public final class Counter extends AbstractReflectionVisitor implements Serializable {

    private Integer termCount = null;

    /**
     * 
     * @param root 
     * @return 
     */
    public synchronized int getTermCount(final Clause root) {
        
        if( termCount == null ){
            termCount = 0;
            visit(root);
        }
        return termCount;
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
    }

    /**
     * 
     * @param clause 
     */
    protected void visitImpl(final DoubleOperatorClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * 
     * @param clause 
     */
    protected void visitImpl(final LeafClause clause) {
        ++termCount;
    }

}