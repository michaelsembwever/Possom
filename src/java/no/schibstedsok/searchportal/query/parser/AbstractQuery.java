/* Copyright (2005-2006) Schibsted Søk AS
 *
 * AbstractQuery.java
 *
 * Created on 12 January 2006, 09:50
 *
 */

package no.schibstedsok.searchportal.query.parser;

import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.finder.Counter;
import no.schibstedsok.searchportal.query.finder.FirstLeafFinder;


/** Abstract helper for implementing a Query class.
 * Handles input of the query string and finding the first leaf clause (term) in the clause hierarchy.
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
        return finder.getFirstLeaf(getRootClause());
    }

    /** TODO comment me. **/
    public int getTermCount() {
        return counter.getTermCount(getRootClause());
    }

    /** TODO comment me. **/
    public boolean isBlank(){
        return false;
    }


}
