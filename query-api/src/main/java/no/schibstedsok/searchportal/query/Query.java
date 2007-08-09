/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.query;

import java.io.Serializable;
import no.schibstedsok.searchportal.query.finder.ParentFinder;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;


/** A Query represents a users inputted query string.
 * The query contains an heirarchy of Clause objects implemented against a visitor pattern
 * that visitors are free to use.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface Query extends Serializable {

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
     ** @return 
     */
    int getTermCount();
    
    /** Is the query blank (or just full of useless symbols). *
     * @return 
     */
    boolean isBlank();

    /**
     * 
     * @return 
     */
    ParentFinder getParentFinder();

    /**
     * 
     * @return 
     */
    TokenEvaluationEngine.State getEvaluationState();
}
