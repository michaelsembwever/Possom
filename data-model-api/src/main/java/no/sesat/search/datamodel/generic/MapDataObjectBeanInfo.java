/* Copyright (2007) Schibsted ASA
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
 * MapDataObjectBeanInfo.java
 *
 * Created on 30 January 2007, 20:51
 *
 */

package no.sesat.search.datamodel.generic;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;

/**
 *
 *
 * @version <tt>$Id$</tt>
 */
public class MapDataObjectBeanInfo extends SimpleBeanInfo{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(MapDataObjectBeanInfo.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of MapDataObjectBeanInfo */
    public MapDataObjectBeanInfo() {
    }

    // Public --------------------------------------------------------

    @Override
    public PropertyDescriptor[] getPropertyDescriptors(){

        return addSingleMappedPropertyDescriptor("value", MapDataObject.class);
    }

    /**
     *
     * @param name
     * @param cls
     * @return
     */
    public static PropertyDescriptor[] addSingleMappedPropertyDescriptor(final String name, final Class<?> cls){

        try{
            final PropertyDescriptor[] existing
                    = Introspector.getBeanInfo(cls, Introspector.IGNORE_ALL_BEANINFO).getPropertyDescriptors();
            // remove this introspection from the cache to avoid reuse of the IGNORE_ALL_BEANINFO result
            Introspector.flushFromCaches(cls);

            final PropertyDescriptor[] result = new PropertyDescriptor[existing.length+2];
            System.arraycopy(existing, 0, result, 0, existing.length);
            result[existing.length] = new MappedPropertyDescriptor(name, cls);
            if( !System.getProperty("java.version").startsWith("1.6") ){
                fixForJavaBug4984912(cls, (MappedPropertyDescriptor)result[existing.length]);
            }

            final String captialised = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            try{
                // try plural with "s" first, then fall back onto "es"
                result[existing.length+1] = new PropertyDescriptor(name + 's', cls, "get" + captialised + 's', null);
            }catch(IntrospectionException ie){
                // who on earth designed the english language!?? :@
                result[existing.length+1] = new PropertyDescriptor(name + "es", cls, "get" + captialised + 's', null);
            }
            return result;

        }catch (IntrospectionException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new IllegalStateException('[' + cls.getSimpleName() + ']' + ex.getMessage(), ex);
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------


    // Private -------------------------------------------------------

    /** Work around for 4984912.
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4984912
     * TODO remove with java6
     **/
    private static void fixForJavaBug4984912(
            final Class<?> cls,
            final MappedPropertyDescriptor mpd) throws IntrospectionException{

        try{
            final String name = mpd.getName();
            final String captialised = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Method m = null;
            try{
                // try plural with "s" first, then fall back onto "es"
                m = cls.getMethod("get" + captialised + 's', new Class[0]);
            }catch(NoSuchMethodException nsme){
                // who on earth designed the english language!?? :@
                m = cls.getMethod("get" + captialised + "es", new Class[0]);
            }
            mpd.setReadMethod(m);

        }catch (NoSuchMethodException ex) {
            LOG.error(ex.getMessage(), ex);
        }catch (SecurityException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    // Inner classes -------------------------------------------------

}
