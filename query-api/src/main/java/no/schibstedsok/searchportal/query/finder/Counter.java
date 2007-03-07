// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.finder;

import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.*;



public final class Counter extends AbstractReflectionVisitor {

    private Integer termCount = null;

    public synchronized int getTermCount(final Clause root) {
        
        if( termCount == null ){
            termCount = 0;
            visit(root);
        }
        return termCount;
    }


    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    protected void visitImpl(final XorClause clause) {
        clause.getFirstClause().accept(this);
    }

    protected void visitImpl(final DoubleOperatorClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    protected void visitImpl(final LeafClause clause) {
        ++termCount;
    }

}