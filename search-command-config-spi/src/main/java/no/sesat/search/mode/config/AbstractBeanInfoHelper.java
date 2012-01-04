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
 */

package no.sesat.search.mode.config;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Helper class to make it easy to non standard PropertyDescriptors in a BeanInfo.
 */
public class AbstractBeanInfoHelper extends SimpleBeanInfo{

    private final ArrayList<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
    private final Class<?> cls;

    /**
     * Populate the descriptors with the standard JavaBean descriptors, using the supplied class.
     *
     * @param cls Class that we are making a BeanInfo for.
     * @throws IntrospectionException
     */
    public AbstractBeanInfoHelper(Class<?> cls) throws IntrospectionException {
        this.cls = cls;
        Collections.addAll(descriptors, Introspector.getBeanInfo(cls, Introspector.IGNORE_ALL_BEANINFO)
                .getPropertyDescriptors());
        // remove this introspection from the cache to avoid reuse of the
        // IGNORE_ALL_BEANINFO result
        // (This sounds like a broken cache, if this is true)
        Introspector.flushFromCaches(cls);
    }

    /**
     * Add a descriptor
     *
     * @param name name of the property
     * @param getter name of the getter, null if none
     * @param setter name of the setter, null if none
     * @throws IntrospectionException
     */
    protected final void add(String name, String getter, String setter) throws IntrospectionException {
        ArrayList<PropertyDescriptor> remove = new ArrayList<PropertyDescriptor>();
        for(PropertyDescriptor d : descriptors) {
            if (d.getName().equals(name)) {
                remove.add(d);
            }
        }
        descriptors.removeAll(remove);
        descriptors.add(new PropertyDescriptor(name, cls, getter, setter));
    }

    @Override
    public final PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] res = new PropertyDescriptor[descriptors.size()];
        return descriptors.toArray(res);
    }
}