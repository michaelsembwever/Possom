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

package no.sesat.search.datamodel.page;


import no.sesat.search.datamodel.generic.MapDataObjectBeanInfo;
import java.beans.PropertyDescriptor;
import org.apache.log4j.Logger;

/**
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class PageDataObjectBeanInfo extends MapDataObjectBeanInfo{

    // Constants -----------------------------------------------------

    private static final Class BEAN_CLASS = PageDataObject.class;

    private static final Logger LOG = Logger.getLogger(PageDataObjectBeanInfo.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of MapDataObjectBeanInfo */
    public PageDataObjectBeanInfo() {
    }

    // Public --------------------------------------------------------

    @Override
    public PropertyDescriptor[] getPropertyDescriptors(){

      return MapDataObjectBeanInfo.addSingleMappedPropertyDescriptor("tab", BEAN_CLASS);
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
