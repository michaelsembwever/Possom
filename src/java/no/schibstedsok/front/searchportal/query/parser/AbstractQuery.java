/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQuery.java
 *
 * Created on 12 January 2006, 09:50
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import no.fast.util.NullObject;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQuery implements Query{
    
    private final String queryStr;
    
    /** Creates a new instance of AbstractQuery */
    protected AbstractQuery(final String queryStr) {
        this.queryStr = queryStr;
    }

    public String getQueryString() {
        return queryStr;
    }

    public Clause getFirstLeafClause() {
        final Clause root = getRootClause();
        final FirstLeafFinder finder = new FirstLeafFinder();
        finder.visit(root);
        return finder.getFirstLeaf();
    }
    
    private static final class FirstLeafFinder extends AbstractReflectionVisitor{
        private boolean searching = true;
        private Clause firstLeaf;

        private static final String ERR_CANNOT_CALL_GETFIRSTLEAF_TIL_SEARCH_OVER
                = "Not allowed to call getFirstLeaf() until search has finished. Start search with visit(Object).";
        
        public Clause getFirstLeaf(){
            if( searching ){
                throw new IllegalStateException(ERR_CANNOT_CALL_GETFIRSTLEAF_TIL_SEARCH_OVER);
            }
            return firstLeaf;
        }
        
        public void visitImpl(AndClause clause) {
            if( searching ){ // still looking
                clause.getFirstClause().accept(this);
            }
        }
        
        public void visitImpl(OrClause clause) {
            if( searching ){ // still looking
                clause.getFirstClause().accept(this);
            }
        }
        
        public void visitImpl(NotClause clause) {
            // this cancels the search for a firstLeafClause...
            searching = false;
        }
        
        public void visitImpl(AndNotClause clause) {
            // this cancels the search for a firstLeafClause...
            searching = false;
        }
        
        public void visitImpl(LeafClause clause) {
            // Bingo! Goto "Go". Collect $200.
            firstLeaf = clause;
            searching = false;
        }
        
    }
}
