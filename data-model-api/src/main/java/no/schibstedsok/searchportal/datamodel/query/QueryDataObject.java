/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * QueryDataObject.java
 *
 * Created on 23 January 2007, 12:42
 *
 */

package no.schibstedsok.searchportal.datamodel.query;

import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;
import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import no.schibstedsok.searchportal.datamodel.access.AccessDisallow;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.query.Query;

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
