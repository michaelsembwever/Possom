/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQuery.java
 *
 * Created on 12 January 2006, 09:50
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.Query;


/** Abstract helper for implementing a Query class.
 * Handles input of the query string and finding the first leaf clause (term) in the clause heirarchy.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public abstract class AbstractQuery implements Query {
    
    private final FirstLeafFinder finder = new FirstLeafFinder();
    private final Counter counter = new Counter();
    
    private final String queryStr;

    /** Creates a new instance of AbstractQuery .
     * @param queryStr the query string as inputted from the user.
     */
    protected AbstractQuery(final String queryStr) {
        this.queryStr = queryStr;
    }

    /**
     * {@inheritDoc}
     */
    public String getQueryString() {
        return queryStr;
    }

    /**
     * {@inheritDoc}
     */
    public Clause getFirstLeafClause() {
        final Clause root = getRootClause();
        finder.visit(root);
        return finder.getFirstLeaf();
    }
    
    public int getTermCount(){
        final Clause root = getRootClause();
        counter.visit(root);
        return counter.getTermCount();
    }

    private static final class FirstLeafFinder extends AbstractReflectionVisitor {
        private boolean searching = true;
        private Clause firstLeaf;

        private static final String ERR_CANNOT_CALL_GETFIRSTLEAF_TIL_SEARCH_OVER
                = "Not allowed to call getFirstLeaf() until search has finished. Start search with visit(Object).";

        public Clause getFirstLeaf() {
            if (searching) {
                throw new IllegalStateException(ERR_CANNOT_CALL_GETFIRSTLEAF_TIL_SEARCH_OVER);
            }
            return firstLeaf;
        }

        public void visitImpl(final AndClause clause) {
            if (searching) { // still looking
                clause.getFirstClause().accept(this);
            }
        }

        public void visitImpl(final OrClause clause) {
            if (searching) { // still looking
                clause.getFirstClause().accept(this);
            }
        }

        public void visitImpl(final NotClause clause) {
            // this cancels the search for a firstLeafClause...
            searching = false;
        }

        public void visitImpl(final AndNotClause clause) {
            // this cancels the search for a firstLeafClause...
            searching = false;
        }

        public void visitImpl(final LeafClause clause) {
            // Bingo! Goto "Go". Collect $200.
            firstLeaf = clause;
            searching = false;
        }

    }
    
    private static final class Counter extends AbstractReflectionVisitor {
        private boolean searching = true;
        private int termCount;

        private static final String ERR_CANNOT_CALL_GETFIRSTLEAF_TIL_SEARCH_OVER
                = "Not allowed to call getTermCount() until search has finished. Start search with visit(Object).";

        public int getTermCount() {
            if (searching) {
                throw new IllegalStateException(ERR_CANNOT_CALL_GETFIRSTLEAF_TIL_SEARCH_OVER);
            }
            return termCount;
        }
        

        public void visitImpl(final OperationClause clause) {
            clause.getFirstClause().accept(this);
        }

        public void visitImpl(final OrClause clause) {
            // Avoid counting the first term in a "Possibility" OrClause
            //  A "Possibility" OrClause is something that the QueryParser 
            //   generates when it detects a possible subclause type, eg PhoneNumberClause or OrganisationNumberClause.
            if( !clause.getFirstClause().getTerm().equals(clause.getSecondClause().getTerm()) ){
                clause.getSecondClause().accept(this);
            }
            clause.getSecondClause().accept(this);
        }

        public void visitImpl(final AndNotClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        public void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        public void visitImpl(final LeafClause clause) {
            ++termCount;
        }

        /**
         * {@inheritDoc}
         */
        public void visit(final Object clause) {
            super.visit(clause);
            searching = false;
        }

    }
}
