/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * LeafClause.java
 *
 * Created on 11 January 2006, 14:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.sesat.search.query;


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
