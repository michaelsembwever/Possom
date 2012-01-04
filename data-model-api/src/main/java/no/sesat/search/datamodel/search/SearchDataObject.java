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
 * SearchDataObject.java
 *
 * Created on 15/05/2007, 11:38:04
 *
 */

package no.sesat.search.datamodel.search;

import no.sesat.search.datamodel.access.AccessAllow;
import no.sesat.search.datamodel.access.ControlLevel;
import static no.sesat.search.datamodel.access.ControlLevel.*;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.query.QueryDataObject;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/** Contains Search Command's manipulated Query and eventual Results information.
 *
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface SearchDataObject {

    SearchConfiguration getConfiguration();

    void setConfiguration(SearchConfiguration configuration);

    /** The command's list of results.
     *
     * @return
     */
    ResultList<ResultItem> getResults();

    /** Set the command's list of results.
     *
     * @param results
     */
    @AccessAllow({ControlLevel.SEARCH_COMMAND_EXECUTION})
    void setResults(ResultList<ResultItem> results);

    // QueryDataObject ------------------------------------------------------------

    /** The command's query. The command may have altered (in a manner noticalbe to the user) the query.
     *
     * @return
     */
    QueryDataObject getQuery();

    /** Set the command's query.
     *
     * @param query
     */
    @AccessAllow({ControlLevel.SEARCH_COMMAND_EXECUTION})
    void setQuery(QueryDataObject query);


}
