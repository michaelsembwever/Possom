package no.schibstedsok.searchportal.query.finder;

import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.parser.*;


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