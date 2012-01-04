/* Copyright (2007-2012) Schibsted ASA
 *   This file is part of Possom.
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
 *
 * Alternation.java
 *
 *
 */

package no.sesat.search.query.parser.alt;

import no.sesat.commons.ioc.BaseContext;
import no.sesat.search.query.Clause;
import no.sesat.search.query.finder.ParentFinder;
import no.sesat.search.query.parser.QueryParser;

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
 *
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
