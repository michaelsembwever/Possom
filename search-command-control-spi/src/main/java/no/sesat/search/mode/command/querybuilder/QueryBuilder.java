/* Copyright (2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */

package no.sesat.search.mode.command.querybuilder;
import java.util.Collection;
import no.sesat.search.query.Clause;
import no.sesat.search.query.Query;
import no.sesat.search.query.Visitor;
import no.sesat.search.query.transform.QueryTransformer;

/** QueryBuilder provides a string representation of a Query Tree against of map of "transformed terms".
 *
 * It is similar in functionality to the QueryTransformer interface
 *  except that it does not transform terms but uses them to build the final string representation.
 *
 * Implementing classes must have a constructor with parameters (Context, QueryBuilderConfig).
 *
 * @version $Id$
 */
public interface QueryBuilder extends Visitor {

    interface Context extends QueryTransformer.Context{
        /** Get the unescaped transformed term for the clause.
         *
         * @param clause
         * @return unescaped transformed term
         */
        String getTransformedTerm(Clause clause);
        /** The collection of words that have special meaning/function within the query string.
         *
         * Each is treated as a regular expressions to match complex words if neccessary.
         *
         * @return collection of reserved words
         */
        Collection<String> getReservedWords();
        /** the SearchConfiguration for the command we are currently running for. **/
    }

    /** The Query String built from the Query's transformed clauses.
     *
     * @return string built from the Query's transformed clauses, or "*".
     */
    String getQueryString();

}
