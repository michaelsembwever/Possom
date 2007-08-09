/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.query.finder;

import java.io.Serializable;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.parser.*;


public final class FirstLeafFinder extends AbstractReflectionVisitor implements Serializable {
    private boolean searching = true;
    private LeafClause firstLeaf;

    public synchronized LeafClause getFirstLeaf(final Clause root) {
        
        visit(root);
        return firstLeaf;
    }

    protected void visitImpl(final OperationClause clause) {
        if (searching) { // still looking
            clause.getFirstClause().accept(this);
        }
    }

    protected void visitImpl(final NotClause clause) {
        // this cancels the search for a firstLeafClause...
        searching = false;
    }

    protected void visitImpl(final AndNotClause clause) {
        // this cancels the search for a firstLeafClause...
        searching = false;
    }

    protected void visitImpl(final LeafClause clause) {
        // Bingo! Goto "Go". Collect $200.
        firstLeaf = clause;
        searching = false;
    }

}