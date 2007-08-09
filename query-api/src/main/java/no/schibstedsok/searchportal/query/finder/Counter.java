/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.query.finder;

import java.io.Serializable;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.*;



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