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

/**
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
