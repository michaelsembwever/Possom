/*
 * BeanDataNodeInvocationHandler.java
 *
 * Created on 23 January 2007, 21:34
 *
 */

package no.schibstedsok.searchportal.datamodel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextSupport;
import java.lang.reflect.Method;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import no.schibstedsok.searchportal.datamodel.generic.DataObject.Property;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
final class BeanDataNodeInvocationHandler<T> implements InvocationHandler {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(BeanDataNodeInvocationHandler.class);

    // Attributes ----------------------------------------------------

    private final Class<T> implementOf;
    private final List<PropertyDescriptor> childPropertyDescriptors = new ArrayList<PropertyDescriptor>();
    private final List<Property> childProperties = new ArrayList<Property>();
    private final BeanDataObjectInvocationHandler<T> dataObject;
    private final BeanContext context = new BeanContextSupport();

    // Static --------------------------------------------------------

    static <T> BeanDataNodeInvocationHandler<T> instanceOf(final Class<T> cls, final Property... properties)
            throws IntrospectionException{

        return new BeanDataNodeInvocationHandler<T>(cls, properties);
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of ProxyBeanDataObject */
    protected BeanDataNodeInvocationHandler(
            final Class<T> cls,
            final Property... allProperties) throws IntrospectionException {

        implementOf = cls;

        final PropertyDescriptor[] allPropertyDescriptors = Introspector.getBeanInfo(cls).getPropertyDescriptors();
        // split properties between children dataObjects and our own properties
        final List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
        final List<Property> properties = new ArrayList<Property>();
        for(PropertyDescriptor property : allPropertyDescriptors){
            if( null != property.getPropertyType().getAnnotation(DataObject.class) ){
                childPropertyDescriptors.add(property);
                // make context to contextChild bindings
                for(Property p : allProperties){
                    if( p.getName().equals(property.getName()) ){
                        if(null != p.getValue()){
                            addChild(p.getValue());
                        }
                        childProperties.add(p);
                        break;
                    }
                }
            }else{
                propertyDescriptors.add(property);
                for(Property p : allProperties){
                    if( p.getName().equals(property.getName()) ){
                        properties.add(p);
                    }
                }
            }
        }

        // delegate our own properties
        dataObject = new BeanDataObjectInvocationHandler(cls, properties.toArray(new Property[properties.size()]));
    }

    // Public --------------------------------------------------------

    public Object invoke(final Object obj, final Method method, final Object[] args) throws Throwable {

        final boolean setter = method.getName().startsWith("set");

        // search for the property applicable within child dataObject properties
        final String propertyName = method.getName().replaceFirst("is|get|set", "");
        for(int i = 0; i < childProperties.size(); ++i){
            final Property p = childProperties.get(i);
            if( p.getName().equalsIgnoreCase(propertyName)){
                if( setter ){

                    // detach the old contextChild
                    removeChild(p.getValue());

                    // set the new child dataObject
                    childProperties.set(i, new Property(p.getName(), args[0]));

                    // add the new contextChild
                    addChild(args[0]);

                }
                return p.getValue();
            }
        }

        // try our own properties
        try{
            dataObject.invoke(obj, method, args);

        }catch(IllegalArgumentException iae){
            LOG.debug("property not from delegate");
        }

        // try pure self methods
        try{
            return method.invoke(this, args);

        }catch(IllegalAccessException iae){
            LOG.info(iae.getMessage(), iae);
        }catch(IllegalArgumentException iae){
            LOG.info(iae.getMessage(), iae);
        }catch(InvocationTargetException ite){
            LOG.info(ite.getMessage(), ite);
        }

        throw new IllegalArgumentException("Method to invoke doesn't map to bean property");
    }

    @Override
    public String toString(){
        return implementOf.getSimpleName()
                + " [Proxy (" + getClass().getSimpleName()
                + ")] w/ " + BeanDataObjectInvocationHandler.toString(childProperties);
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void addChild(final Object obj){

        final BeanDataObjectInvocationHandler<?> childsNewHandler
                    = (BeanDataObjectInvocationHandler<?>) Proxy.getInvocationHandler(obj);
        context.add(childsNewHandler.getBeanContextChild());
    }

    private void removeChild(final Object obj){

        final BeanDataObjectInvocationHandler<?> childsOldHandler
                    = (BeanDataObjectInvocationHandler<?>) Proxy.getInvocationHandler(obj);
        context.remove(childsOldHandler.getBeanContextChild());
    }

    // Inner classes -------------------------------------------------



}
