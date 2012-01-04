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
/*
 * MapDataObjectBeanInfo.java
 *
 * Created on 30 January 2007, 20:51
 *
 */

package no.sesat.search.datamodel.request;


import java.beans.PropertyDescriptor;
import no.sesat.search.datamodel.generic.MapDataObjectBeanInfo;
import org.apache.log4j.Logger;

/**
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class ParametersDataObjectBeanInfo extends MapDataObjectBeanInfo{

    // Constants -----------------------------------------------------

    private static final Class BEAN_CLASS = ParametersDataObject.class;

    private static final Logger LOG = Logger.getLogger(ParametersDataObjectBeanInfo.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of MapDataObjectBeanInfo */
    public ParametersDataObjectBeanInfo() {
    }

    // Public --------------------------------------------------------

    /**
     *
     * @return
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors(){

        return MapDataObjectBeanInfo.addSingleMappedPropertyDescriptor("value", BEAN_CLASS);
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
