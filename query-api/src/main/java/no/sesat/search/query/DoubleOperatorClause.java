/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 * DoubleOperatorClause.java
 *
 * Created on 11 January 2006, 14:16
 *
 */

package no.sesat.search.query;


/** An operation clause. A join between two other clauses.
 *
 * @version $Id: OperationClause.java 3359 2006-08-03 08:13:22Z mickw $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface DoubleOperatorClause extends OperationClause {
    /**
     * Get the second clause.
     *
     * @return the second clause.
     */
    Clause getSecondClause();

}
