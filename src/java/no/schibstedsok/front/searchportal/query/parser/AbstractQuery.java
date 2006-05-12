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
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.Query;


/** Abstract helper for implementing a Query class.
 * Handles input of the query string and finding the first leaf clause (term) in the clause heirarchy.
 * Is thread safe. No methods return null.
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
    public LeafClause getFirstLeafClause() {
        return finder.getFirstLeaf();
    }

    public int getTermCount() {
        return counter.getTermCount();
    }

    public boolean isBlank(){
        return false;
    }
    
    private final class FirstLeafFinder extends AbstractReflectionVisitor {
        private boolean searching = true;
        private LeafClause firstLeaf;

        public synchronized LeafClause getFirstLeaf() {
            if( firstLeaf == null ){
                // hasn't been run yet.
                visit(getRootClause());
            }
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

    private final class Counter extends AbstractReflectionVisitor {

        private int termCount = 0;

        public synchronized int getTermCount() {
            if(termCount == 0){
                // hasn't been run yet.
                visit(getRootClause());
            }
            return termCount;
        }


        protected void visitImpl(final OperationClause clause) {
            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final OrClause clause) {
            // Avoid counting the first term in a "Possibility" OrClause
            //  A "Possibility" OrClause is something that the QueryParser
            //   generates when it detects a possible subclause type, eg PhoneNumberClause or OrganisationNumberClause.
            if (!clause.getFirstClause().getTerm().equals(clause.getSecondClause().getTerm())) {
                clause.getSecondClause().accept(this);
            }
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final AndNotClause clause) {
            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final LeafClause clause) {
            ++termCount;
        }

    }

}
