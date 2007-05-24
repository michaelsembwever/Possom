/*
 * Alternation.java
 *
 *
 */

package no.schibstedsok.searchportal.query.parser.alt;

import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.finder.ParentFinder;
import no.schibstedsok.searchportal.query.parser.QueryParser;

/** An Alternation is a query manipulation occurring on the query object after parsing has finished.
 * Alernations are cpu expensive as Clauses are immutable and every change during the alternation
 *  results in a new Clause being constructed. A change in a clause deep within the query tree requires every parent
 *  in it's ancestry line back to the query's root clause being reconstructed.
 * The value of alternation comes into play where the query parser cannot construct such a result, and doing the 
 *  manipulation on-the-fly will likely occur multiple times during the request.
 * It's typical that the alternations do not directly manipulate any clause but constructed an alternative clause to it
 *  and replaces the original clause with an XorClause that contains both the original and the new alternative.
 *  In these cases it is also typical that only one type of XorClause.Hint is used through that alternation process.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public interface Alternation {
    
    /** Context to work within. **/
    public interface Context extends BaseContext, QueryParser.Context {
        /**
         * 
         * @return 
         */
        ParentFinder getParentFinder();
    }    
    
    /** Perform the alternation.
     * 
     * @param clause 
     * @return 
     */
    Clause alternate(Clause clause);
}
