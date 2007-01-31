/*
 * MapDataObjectBeanInfo.java
 *
 * Created on 30 January 2007, 20:51
 *
 */

package no.schibstedsok.searchportal.datamodel.generic;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
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
    
    public PropertyDescriptor[] getPropertyDescriptors(){
        
        return addSingleMappedPropertyDescriptor("value", MapDataObject.class);
    }
        
    public static PropertyDescriptor[] addSingleMappedPropertyDescriptor(final String name, final Class<?> cls){
        
        try{
            final PropertyDescriptor[] existing 
                    = Introspector.getBeanInfo(cls, Introspector.IGNORE_ALL_BEANINFO).getPropertyDescriptors();
            
            final PropertyDescriptor[] result = new PropertyDescriptor[existing.length+2];
            System.arraycopy(existing, 0, result, 0, existing.length);
            result[existing.length] = new MappedPropertyDescriptor(name, cls);
            
            final String captialised = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            result[existing.length+1] = new PropertyDescriptor(name + 's', cls, "get" + captialised + 's', null);
            return result;
            
        }catch (IntrospectionException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new IllegalStateException('[' + cls.getSimpleName() + ']' + ex.getMessage(), ex);
        }
    }
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------

    
    // Private -------------------------------------------------------
    
    
    
    // Inner classes -------------------------------------------------
    
}
