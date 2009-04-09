/* Copyright (2007-2008) Schibsted ASA
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
 */
/*
 * MapDataObject.java
 *
 * Created on 23 January 2007, 13:31
 *
 */

package no.sesat.search.datamodel.generic;

import no.sesat.search.datamodel.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * This helper class provides a utility implementation of the
 * no.sesat.search.datamodel.MapDataObject interface.
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
 *
 * @version <tt>$Id$</tt>
 */
@DataObject
public final class MapDataObjectSupport<V> implements MapDataObject<V>{

    // Most MapDataObjectSupport instances only contain an item or two.
    // Max currency in any mode is typically ~20, but unlikely for even two threads to update at the same time.
    private final Map<String,V> map = new ConcurrentHashMap<String,V>(5, 0.75f, 10){

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
