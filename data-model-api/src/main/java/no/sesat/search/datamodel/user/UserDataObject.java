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
 */
/*
 * UserDataObject.java
 *
 * Created on 23 January 2007, 12:36
 *
 */

package no.sesat.search.datamodel.user;

import java.io.Serializable;
import java.util.Map;
import no.sesat.search.datamodel.generic.DataNode;
import no.sesat.search.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.user.User;

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
    User getUser();
    
    /**
     * 
     * @param user 
     */
    void setUser(User user);

//    Map<String,LocationDataObject> getLocations();
//
//    LocationDataObject getLocation(final String key);
//
//    LocationDataObject putLocation(final String key, final LocationDataObject value);
}
