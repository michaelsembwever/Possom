// Copyright (2007) Schibsted Søk AS
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
    
    User getUser();
    
    void setUser(User user);

//    Map<String,LocationDataObject> getLocations();
//
//    LocationDataObject getLocation(final String key);
//
//    LocationDataObject putLocation(final String key, final LocationDataObject value);
}
