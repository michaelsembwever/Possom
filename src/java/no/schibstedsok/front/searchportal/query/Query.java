/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query;


/** A Query represents a users inputted query string.
 * The query contains an heirarchy of Clause objects implemented against a visitor pattern
 * that visitors are free to use.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Query {

    /** The root clause to the clause heirarchy.
     * Will always be an operation clause if more than one term exists in the query.
     * @return the root clause.
     */
    Clause getRootClause();

    /** The schibstedsøk style string formatted representation of this query.
     *
     * @return schibstedsøk style string formatted representation of this query.
     */
    String getQueryString();

    /** The first term (leaf clause) in the query.
     *
     * @return the first leaf clause.
     */
    Clause getFirstLeafClause();
    
    /** Return the number of terms in this query.
     * Terms are represented by LeafClauses.
     **/
    int getTermCount();

}
