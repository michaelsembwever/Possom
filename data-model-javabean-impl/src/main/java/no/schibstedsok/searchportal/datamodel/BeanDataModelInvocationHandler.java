// Copyright (2007) Schibsted Søk AS
/*
 * BeanDataModelInvocationHandler.java
 *
 * Created on 2 February 2007, 11:27
 *
 */

package no.schibstedsok.searchportal.datamodel;

import java.beans.IntrospectionException;
import no.schibstedsok.searchportal.datamodel.access.ControlLevel;
import no.schibstedsok.searchportal.datamodel.generic.DataObject.Property;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class BeanDataModelInvocationHandler extends BeanDataNodeInvocationHandler<DataModel>{
    
    // Constants -----------------------------------------------------
    
    
    // Attributes ----------------------------------------------------
    
    private ControlLevel controlLevel = ControlLevel.DATA_MODEL_CONSTRUCTION;
    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of BeanDataModelInvocationHandler 
     * @param allProperties 
     * @throws java.beans.IntrospectionException 
     */
    protected BeanDataModelInvocationHandler(
            final PropertyInitialisor properties) throws IntrospectionException {
        
        super(DataModel.class, properties);
    }
    
    // Public --------------------------------------------------------
    
    
    // Package protected ---------------------------------------------
    
    void incrementControlLevel(){
        controlLevel = controlLevel.next();
    }
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}
