/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
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
 *
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
