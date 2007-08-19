/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.query.finder;

import no.sesat.search.query.Clause;
import no.sesat.search.query.DoubleOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.parser.*;


final class ChildFinder extends AbstractReflectionVisitor {
    
    private boolean found;
    private Clause child = null;

    public synchronized boolean childExists(final OperationClause parent, final Clause child) {
        
        found = false;
        this.child = child;
        visit(parent);
        return found;
    }

    protected void visitImpl(final DoubleOperatorClause clause) {
        if (!found) { // still looking
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
    }
    
    protected void visitImpl(final OperationClause clause) {
        if (!found ) { // still looking
            clause.getFirstClause().accept(this);
        }
    }

    protected void visitImpl(final LeafClause clause) {
        
        found = clause == child;
    }

}