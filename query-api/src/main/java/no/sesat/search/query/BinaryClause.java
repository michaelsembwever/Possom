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
 * DoubleOperatorClause.java
 *
 * Created on 11 January 2006, 14:16
 *
 */

package no.sesat.search.query;


/** An operation clause. A join between two other clauses.
 *
 * @version $Id: OperationClause.java 3359 2006-08-03 08:13:22Z mickw $
 *
 */
public interface BinaryClause extends UnaryClause {
    /**
     * Get the second clause.
     *
     * @return the second clause.
     */
    Clause getSecondClause();

}
