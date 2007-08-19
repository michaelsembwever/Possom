/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * ParametersMapDataObject.java
 *
 * Created on 23 January 2007, 13:51
 *
 */

package no.sesat.search.datamodel.request;

import java.util.Map;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.MapDataObject;
import no.sesat.search.datamodel.access.AccessAllow;
import no.sesat.search.datamodel.access.AccessDisallow;
import no.sesat.search.datamodel.generic.DataNode;
import no.sesat.search.datamodel.generic.StringDataObject;
import static no.sesat.search.datamodel.access.ControlLevel.*;

/** The ParameterDataObject class is a container for all request based information.
 *  This mainly includes parameters, but also headers and attributes.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataNode
public interface ParametersDataObject extends MapDataObject<StringDataObject>{

    Map<String,StringDataObject> getValues();

    /**
     * 
     * @param key 
     * @return 
     */
    StringDataObject getValue(final String key);

    /**
     * 
     * @param key 
     * @param value 
     */
    @AccessAllow({})
    void setValue(final String key, final StringDataObject value);
    
    /** The UniqueId is used to trace one request from it's origin (apace or tomcat) down through each command and 
     * into the indexes.
     * 
     * @return the uniqueId
     */
    String getUniqueId();
}
