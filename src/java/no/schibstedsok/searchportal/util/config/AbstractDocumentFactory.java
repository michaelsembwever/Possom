/*
 * AbstractDocumentFactory.java
 *
 * Created on 21. april 2006, 13:17
 *
 */

package no.schibstedsok.searchportal.util.config;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;


/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractDocumentFactory {


    // Constants -----------------------------------------------------
    private static final Logger LOG = Logger.getLogger(AbstractDocumentFactory.class);
    private static final String ERR_FILL_PROPERTY = "Could not set javabean property";
    private static final String DEBUG_UNABLE_TO_INHERIT_VALUE_1 = "Could not inherit value ";
    private static final String DEBUG_UNABLE_TO_INHERIT_VALUE_2 = " from ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of AbstractDocumentFactory */
    protected AbstractDocumentFactory() {
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------



    protected enum ParseType{
        /** TODO comment me. **/
        Boolean, 
        Int, 
        Float, 
        String, 
        Property
    };
    
    /** TODO implement Type.Property. **/
    protected static final void fillBeanProperty(
            final Object beanObj, 
            final Object beanParent, 
            final String property,
            final ParseType type,
            final Element element,
            final String def){

      try {
            
            final StringBuilder attributeName = new StringBuilder(property);
            for(int i = 0; i < attributeName.length(); ++i){
                final char c = attributeName.charAt(i);
                if(Character.isUpperCase(c)){
                    attributeName.replace(i, i+1, "-" + Character.toLowerCase(c));
                    ++i;
                }
            }
            
            final String attributeValue = element.getAttribute(attributeName.toString());
            
            if(attributeValue != null && attributeValue.length()>0 ){
                BeanUtils.setProperty(beanObj, property, attributeValue);
            }else{
                
                String inheritedValue = null;
                if( beanParent != null ){
                    try{
                        inheritedValue = BeanUtils.getProperty(beanParent, property);
                    }catch(NoSuchMethodException nsme){
                        // normal occurance when the bean to inherit values from is not of the same class
                        LOG.debug(DEBUG_UNABLE_TO_INHERIT_VALUE_1 + property
                                + DEBUG_UNABLE_TO_INHERIT_VALUE_1 + beanParent);
                    }
                }
                
                if(inheritedValue != null && inheritedValue.length()>0 ){
                    BeanUtils.setProperty(beanObj, property, inheritedValue);
                }else{
                    BeanUtils.setProperty(beanObj, property, def);
                }
            }
            
        } catch (IllegalAccessException ex) {
            LOG.error(ERR_FILL_PROPERTY, ex);
        } catch (InvocationTargetException ex) {
            LOG.error(ERR_FILL_PROPERTY, ex);
        }
    }
    
    /** @deprecated use fillBeanProperty instead. **/
    protected static final boolean parseBoolean(final String s, final boolean def){
        return s.trim().length() == 0 ? def : Boolean.parseBoolean(s);
    }
    
    /** @deprecated use fillBeanProperty instead. **/
    protected static final float parseFloat(final String s, final float def){
        return s.trim().length() == 0 ? def : Float.parseFloat(s);
    }

    /** @deprecated use fillBeanProperty instead. **/
    protected static final int parseInt(final String s, final int def){
        return s.trim().length() == 0 ? def : Integer.parseInt(s);
    }
    
    /** @deprecated use fillBeanProperty instead. **/
    protected static final String parseString(final String s, final String def){
        return s.trim().length() == 0 ? def : s;
    }
    

    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    
}
