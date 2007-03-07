// Copyright (2007) Schibsted Søk AS
/*
 * MapDataObject.java
 *
 * Created on 23 January 2007, 13:31
 *
 */

package no.schibstedsok.searchportal.datamodel.generic;

import java.io.Serializable;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.*;
import java.util.Map;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface MapDataObject<V> extends Serializable {

    /**
     * Access to whole map is through a Collections.unmodifiable(map) copy.
     **/
    Map<String,V> getValues();

    V getValue(final String key);

    void setValue(final String key, final V value);


}
