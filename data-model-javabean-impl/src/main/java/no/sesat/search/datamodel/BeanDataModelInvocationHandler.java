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
 * BeanDataModelInvocationHandler.java
 *
 * Created on 2 February 2007, 11:27
 *
 */

package no.sesat.search.datamodel;

import java.beans.IntrospectionException;
//import java.beans.beancontext.BeanContextSupport;
import no.sesat.search.datamodel.access.ControlLevel;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
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
    
    protected static final class DataModelBeanContextSupport extends BeanContextSupport{
        
        final Object dataModelLock = new Boolean(true); // needs to be serialisable

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
