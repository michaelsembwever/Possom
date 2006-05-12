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

    /** The original string the user entered for the search.
     * This string should never be used programmatically or passed to search indexes.
     * It is only intended for display and feedback.
     *
     * @return the original user's query.
     */
    String getQueryString();

    /** The first term (leaf clause) in the query.
     *
     * @return the first leaf clause.
     */
    LeafClause getFirstLeafClause();

    /** Return the number of terms in this query.
     * Terms are represented by LeafClauses.
     **/
    int getTermCount();
    
    /** Is the query blank (or just full of useless symbols). **/
    boolean isBlank();

}
