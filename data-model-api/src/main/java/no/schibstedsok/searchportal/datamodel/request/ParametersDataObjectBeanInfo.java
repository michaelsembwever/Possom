/*
 * MapDataObjectBeanInfo.java
 *
 * Created on 30 January 2007, 20:51
 *
 */

package no.schibstedsok.searchportal.datamodel.request;

import no.schibstedsok.searchportal.datamodel.generic.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Arrays;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class ParametersDataObjectBeanInfo extends MapDataObjectBeanInfo{
    
    // Constants -----------------------------------------------------
    
    private static final Class BEAN_CLASS = ParametersDataObject.class;
    
    private static final Logger LOG = Logger.getLogger(MapDataObjectBeanInfo.class);
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of MapDataObjectBeanInfo */
    public ParametersDataObjectBeanInfo() {
    }
    
    // Public --------------------------------------------------------
    
    public PropertyDescriptor[] getPropertyDescriptors(){
        
        
        
        return MapDataObjectBeanInfo.addSingleMappedPropertyDescriptor("value", BEAN_CLASS);
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}
