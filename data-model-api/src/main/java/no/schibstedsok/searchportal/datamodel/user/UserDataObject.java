/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
/*
 * UserDataObject.java
 *
 * Created on 23 January 2007, 12:36
 *
 */

package no.schibstedsok.searchportal.datamodel.user;

import java.io.Serializable;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.generic.DataNode;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
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
