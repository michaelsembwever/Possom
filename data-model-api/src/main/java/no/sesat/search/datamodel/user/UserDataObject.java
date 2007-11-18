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
 **
 * UserDataObject.java
 *
 * Created on 23 January 2007, 12:36
 *
 */

package no.sesat.search.datamodel.user;

import java.io.Serializable;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.user.BasicUser;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
//@DataNode
@DataObject
public interface UserDataObject extends Serializable {

    // Locations -- Delegate to Map<String,LocationDataObject> -------------------------------
    
    /**
     * 
     * @return 
     */
    BasicUser getUser();
    
    /**
     * 
     * @param user 
     */
    void setUser(final BasicUser user);

//    Map<String,LocationDataObject> getLocations();
//
//    LocationDataObject getLocation(final String key);
//
//    LocationDataObject putLocation(final String key, final LocationDataObject value);
}
