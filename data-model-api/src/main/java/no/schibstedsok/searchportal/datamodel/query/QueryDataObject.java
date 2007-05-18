/* Copyright (2007) Schibsted Søk AS
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

    @AccessAllow({REQUEST_CONSTRUCTION, RUNNING_QUERY_CONSTRUCTION})
    String getString();

}
