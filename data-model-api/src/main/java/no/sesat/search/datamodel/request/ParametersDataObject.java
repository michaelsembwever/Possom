/* Copyright (2007) Schibsted Søk AS
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
 *
 * ParametersMapDataObject.java
 *
 * Created on 23 January 2007, 13:51
 *
 */

package no.sesat.search.datamodel.request;

import java.util.Map;
import no.sesat.search.datamodel.generic.MapDataObject;
import no.sesat.search.datamodel.access.AccessAllow;
import no.sesat.search.datamodel.generic.DataNode;
import no.sesat.search.datamodel.generic.StringDataObject;

/** The ParameterDataObject class is a container for all request based information.
 *  This mainly includes parameters, but also headers and attributes.
 *
 *
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
