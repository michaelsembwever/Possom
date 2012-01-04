/* Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
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
 *
 */
public interface LeafClause extends Clause {
    /** Leaf clauses can be specified with a field.
     * For example: "firstname:magnus" gives a LeafClause with term = magnus and field = firstname.
     * @return the field for this Clause.
     **/
    String getField();
}
