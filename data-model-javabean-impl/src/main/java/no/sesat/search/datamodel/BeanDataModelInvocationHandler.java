/* Copyright (2007-2012) Schibsted ASA
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
 * BeanDataModelInvocationHandler.java
 *
 * Created on 2 February 2007, 11:27
 *
 */

package no.sesat.search.datamodel;

import java.beans.IntrospectionException;
//import java.beans.beancontext.BeanContextSupport;
import java.io.Serializable;
import no.sesat.search.datamodel.access.ControlLevel;
import org.apache.log4j.Logger;

/**
 *
 *
 * @version <tt>$Id$</tt>
 */
final class BeanDataModelInvocationHandler extends BeanDataNodeInvocationHandler<DataModel>{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(BeanDataModelInvocationHandler.class);


    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of BeanDataModelInvocationHandler
     * @param allProperties
     * @throws java.beans.IntrospectionException
     */
    protected BeanDataModelInvocationHandler(final PropertyInitialisor properties) throws IntrospectionException {

        super(DataModel.class, null, new DataModelBeanContextSupport(), properties);
    }

    // Public --------------------------------------------------------


    // Package protected ---------------------------------------------

    void setControlLevel(final ControlLevel controlLevel){

        ((DataModelBeanContextSupport)context).setControlLevel(controlLevel);
    }

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

    static final class DataModelBeanContextSupport extends BeanContextSupport{

        final Object dataModelLock = new Serializable() {};

        private ControlLevel controlLevel = ControlLevel.DATA_MODEL_CONSTRUCTION;

        ControlLevel getControlLevel(){
            return controlLevel;
        }

        void setControlLevel(final ControlLevel controlLevel){

            this.controlLevel = controlLevel;
            LOG.trace("Incrementing ControlLevel to " + controlLevel);
        }
    }
}
