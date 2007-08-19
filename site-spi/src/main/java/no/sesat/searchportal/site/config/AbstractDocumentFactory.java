/*
 * AbstractDocumentFactory.java
 *
 * Created on 21. april 2006, 13:17
 *
 */

package no.sesat.searchportal.site.config;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;


/** Utility class for deserialising from an xml document.
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



    /**
     * 
     */
    public enum ParseType{
        /** **/
        Boolean,
        /**
         * 
         */
        Int,
        /**
         * 
         */
        Float,
        /**
         * 
         */
        String,
        /**
         * 
         */
        Property
    };

    /** TODO implement Type.Property. *
     * @param beanObj 
     * @param beanParent 
     * @param property 
     * @param type 
     * @param element 
     * @param def 
     */
    public static final void fillBeanProperty(
            final Object beanObj,
            final Object beanParent,
            final String property,
            final ParseType type,
            final Element element,
            final String def){

      try {

            final String attributeValue = element.getAttribute(beanToXmlName(property));

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

    /***
    * <p>The words within the bean name are deduced assuming the
    * first-letter-capital (for example camel's hump) naming convention. For
    * example, the words in <code>FooBar</code> are <code>foo</code>
    * and <code>bar</code>.</p>
    *
    * <p>Then the {@link #getSeparator} property value is inserted so that it separates
    * each word.</p>
    *
    * @param beanName The name string to convert.  If a JavaBean
    * class name, should included only the last part of the name
    * rather than the fully qualified name (e.g. FooBar rather than
    * org.example.FooBar).
    * @return the bean name converted to either upper or lower case with words separated
    * by the separator.
    **/
    public static String beanToXmlName(final String beanName){

        final StringBuilder xmlName = new StringBuilder(beanName);
        for(int i = 0; i < xmlName.length(); ++i){
            final char c = xmlName.charAt(i);
            if(Character.isUpperCase(c)){
                xmlName.replace(i, i+1, "-" + Character.toLowerCase(c));
                ++i;
            }
        }
        return xmlName.toString();
    }

    /** The reverse transformation to beanToXmlName(string). *
     * @param xmlName 
     * @return 
     */
    public static String xmlToBeanName(final String xmlName){

        final StringBuilder beanName = new StringBuilder(xmlName);
        for(int i = 0; i < beanName.length(); ++i){
            final char c = beanName.charAt(i);
            if('-' == c){
                beanName.replace(i, i+2, String.valueOf(Character.toUpperCase(beanName.charAt(i+1))));
                ++i;
            }
        }
        return beanName.toString();
    }

    /** try to use fillBeanProperty instead. *
     * @param s 
     * @param def 
     * @return 
     */
    public static final boolean parseBoolean(final String s, final boolean def){
        return s.trim().length() == 0 ? def : Boolean.parseBoolean(s);
    }

    /** try to use fillBeanProperty instead. *
     * @param s 
     * @param def 
     * @return 
     */
    public static final float parseFloat(final String s, final float def){
        return s.trim().length() == 0 ? def : Float.parseFloat(s);
    }

    /** try to use fillBeanProperty instead. *
     * @param s 
     * @param def 
     * @return 
     */
    public static final int parseInt(final String s, final int def){
        return s.trim().length() == 0 ? def : Integer.parseInt(s);
    }

    /** try to use fillBeanProperty instead. *
     * @param s 
     * @param def 
     * @return 
     */
    public static final String parseString(final String s, final String def){
        return s.trim().length() == 0 ? def : s;
    }


    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
