// Copyright (2007) Schibsted Søk AS
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
