/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.searchportal.query.finder;

import java.io.Serializable;
import no.sesat.searchportal.query.AndNotClause;
import no.sesat.searchportal.query.Clause;
import no.sesat.searchportal.query.LeafClause;
import no.sesat.searchportal.query.NotClause;
import no.sesat.searchportal.query.OperationClause;
import no.sesat.searchportal.query.parser.*;


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