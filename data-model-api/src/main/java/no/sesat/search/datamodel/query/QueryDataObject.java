/* Copyright (2007) Schibsted Søk AS
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
 *
 * QueryDataObject.java
 *
 * Created on 23 January 2007, 12:42
 *
 */

package no.sesat.search.datamodel.query;

import no.sesat.search.datamodel.generic.StringDataObject;
import static no.sesat.search.datamodel.access.ControlLevel.*;
import no.sesat.search.datamodel.access.AccessAllow;
import no.sesat.search.datamodel.access.AccessDisallow;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.query.Query;

/** The QueryDataObject is the datamodel's container around the user inputted query string 
 * and the same query parsed into a Query tree.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface QueryDataObject extends StringDataObject{

    /**
     * 
     * @return 
     */
    @AccessDisallow(REQUEST_CONSTRUCTION)
    Query getQuery();

    /**
     * 
     * @param query 
     */
    @AccessAllow(RUNNING_QUERY_CONSTRUCTION)
    void setQuery(Query query);

    @AccessAllow({SEARCH_COMMAND_CONSTRUCTION, SEARCH_COMMAND_EXECUTION, RUNNING_QUERY_RESULT_HANDLING})
    String getString();

}
