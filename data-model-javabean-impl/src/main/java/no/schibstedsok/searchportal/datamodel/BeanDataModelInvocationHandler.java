/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
/*
 * BeanDataModelInvocationHandler.java
 *
 * Created on 2 February 2007, 11:27
 *
 */

package no.schibstedsok.searchportal.datamodel;

import java.beans.IntrospectionException;
import java.beans.beancontext.BeanContextSupport;
import no.schibstedsok.searchportal.datamodel.access.ControlLevel;
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
        
        super(DataModel.class, new DataModelBeanContextSupport(), properties);
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
