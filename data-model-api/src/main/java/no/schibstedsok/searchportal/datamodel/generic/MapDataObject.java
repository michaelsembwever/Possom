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
 * @param V 
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public interface MapDataObject<V> extends Serializable {

    /**
     * Access to whole map is through a Collections.unmodifiable(map) copy.
     ** @return 
     */
    Map<String,V> getValues();

    /** Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * 
     * @param key 
     * @return 
     */
    V getValue(final String key);

    /** Associates the specified value with the specified key in this map (optional operation). 
     * If the map previously contained a mapping for the key, the old value is replaced by the specified value. 
     * (A map m is said to contain a mapping for a key k if and only if m.containsKey(k) would return true.)<br/><br/>
     * 
     * If the value is null remove(key) is called on the underlying map.
     *
     * @param key 
     * @param value 
     */
    void setValue(final String key, final V value);


}
