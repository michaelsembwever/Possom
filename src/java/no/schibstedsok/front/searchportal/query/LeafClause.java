/* Copyright (2005-2006) Schibsted Søk AS
 * LeafClause.java
 *
 * Created on 11 January 2006, 14:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query;


/** A clause representing a leaf. That is, a term or word in the query string.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface LeafClause extends Clause {
    /** Leaf clauses can be specified with a field.
     * For example: "firstname:magnus" gives a LeafClause with term = magnus and field = firstname.
     * @return the field for this Clause.
     **/
    String getField();
}
