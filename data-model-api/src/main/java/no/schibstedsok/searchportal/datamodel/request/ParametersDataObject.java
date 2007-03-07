// Copyright (2007) Schibsted Søk AS
/*
 * ParametersMapDataObject.java
 *
 * Created on 23 January 2007, 13:51
 *
 */

package no.schibstedsok.searchportal.datamodel.request;

import java.util.Map;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.generic.MapDataObject;
import no.schibstedsok.searchportal.datamodel.access.AccessAllow;
import no.schibstedsok.searchportal.datamodel.access.AccessDisallow;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import static no.schibstedsok.searchportal.datamodel.access.ControlLevel.*;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface ParametersDataObject extends MapDataObject<StringDataObject>{

    @AccessDisallow(VIEW_CONSTRUCTION)
    Map<String,StringDataObject> getValues();

    @AccessDisallow(VIEW_CONSTRUCTION)
    StringDataObject getValue(final String key);

    @AccessAllow({})
    void setValue(final String key, final StringDataObject value);
    
    @AccessAllow({REQUEST_CONSTRUCTION, VIEW_CONSTRUCTION})
    String getContextPath();
    
    @AccessAllow({})
    void setContextPath(final String contextPath);
}
