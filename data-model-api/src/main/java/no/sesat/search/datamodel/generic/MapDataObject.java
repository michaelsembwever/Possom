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
 */
/*
 * MapDataObject.java
 *
 * Created on 23 January 2007, 13:31
 *
 */

package no.sesat.search.datamodel.generic;

import java.io.Serializable;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.*;
import java.util.Map;

/**
 *
 * @param V
 *
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
