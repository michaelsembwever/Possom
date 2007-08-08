// Copyright (2007) Schibsted Søk AS
/*
 * MapDataObject.java
 *
 * Created on 23 January 2007, 13:31
 *
 */

package no.schibstedsok.searchportal.datamodel.generic;

import no.schibstedsok.searchportal.datamodel.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * This helper class provides a utility implementation of the
 * no.schibstedsok.searchportal.datamodel.MapDataObject interface.
 * </p>
 * <p>
 * Since this class directly implements the MapDataObject interface, the class
 * can, and is intended to be used either by subclassing this implementation,
 * or via ad-hoc delegation of an instance of this class from another.
 * </p>
 *
 * <b>Synchronised implementation of MapDataObject</b> through underlyng usage of ConcurrentHashMap.
 * //Uses a ReentrantReadWriteLock in a delegated HashMap in preference to a Hashtable to maximise performance.
 * //Access to the whole map is through a Collections.unmodifiable(map) defensive copy.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
public final class MapDataObjectSupport<V> implements MapDataObject<V>{

    private final Map<String,V> map = new ConcurrentHashMap<String,V>(){

        @Override
        public V put(final String key, final V value){

            return null == value
                ? super.remove(key)
                : super.put(key, value);
        }
    };

    public MapDataObjectSupport(final Map<String,V> map){
        
        if( null != map ){
            this.map.putAll(map);
        }
    }

    public V getValue(final String key){

        return null != key ? map.get(key) : null;
    }

    public void setValue(final String key, final V value){

        map.put(key, value);
    }

    public Map<String, V> getValues() {

        //XXX return Collections.unmodifiableMap(map);
        return map;
    }

}
