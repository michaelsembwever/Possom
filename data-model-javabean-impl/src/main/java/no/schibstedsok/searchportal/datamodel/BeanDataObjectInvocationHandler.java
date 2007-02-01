/*
 * BeanDataObjectInvocationHandler.java
 *
 * Created on 23 January 2007, 21:34
 *
 */

package no.schibstedsok.searchportal.datamodel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.searchportal.datamodel.generic.DataObject.Property;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
@DataObject
final class BeanDataObjectInvocationHandler<T> implements InvocationHandler {

    // Constants -----------------------------------------------------

    private static final Map<Property[], WeakReference<BeanDataObjectInvocationHandler<?>>> instances
            = new HashMap<Property[], WeakReference<BeanDataObjectInvocationHandler<?>>>();

    private static final ReentrantReadWriteLock instancesLock = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(BeanDataObjectInvocationHandler.class);

    // Attributes ----------------------------------------------------

    private final Class<T> implementOf;
    private final boolean immutable;
    private final List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
    // properties: the only part of this class that be immutable and reused
    private final List<Property> properties = new ArrayList<Property>(); 

    private final BeanContextChild contextChild = new BeanContextChildSupport();

    // Static --------------------------------------------------------

    static <T> BeanDataObjectInvocationHandler<T> instanceOf(final Class<T> cls, final Property... properties)
            throws IntrospectionException{


        BeanDataObjectInvocationHandler instance;
        if(isImmutable(cls)){
            try{
                instancesLock.readLock().lock();
                instance = instances.get(properties).get();
            }finally{
                instancesLock.readLock().unlock();
            }
            if(null == instance){
                try{
                    instancesLock.writeLock().lock();
                    instance = new BeanDataObjectInvocationHandler<T>(cls, properties);
                    instances.put(properties, new WeakReference(instance));
                }finally{
                    instancesLock.writeLock().unlock();
                }
            }
        }else{
            instance = new BeanDataObjectInvocationHandler<T>(cls, properties);
        }

        return instance;
    }

    static String toString(final List<Property> properties){

        final StringBuilder builder = new StringBuilder("{");
        for( Property property : properties){
            builder.append(property.getName() + ':' + property.getValue() + ';');
        }
        builder.append('}');
        return builder.toString();
    }


    // Constructors --------------------------------------------------

    /** Creates a new instance of ProxyBeanDataObject */
    protected BeanDataObjectInvocationHandler(
            final Class<T> cls,
            final Property... properties)
                throws IntrospectionException {

        implementOf = cls;
        for(Property p : properties){
            this.properties.add(p);
        }
        for(PropertyDescriptor pd : Introspector.getBeanInfo(cls).getPropertyDescriptors()){
            this.propertyDescriptors.add(pd);
        }

        this.immutable = isImmutable(cls);
    }



    // Public --------------------------------------------------------

    public Object invoke(final Object obj, final Method method, final Object[] args) throws Throwable {

        final boolean setter = method.getName().startsWith("set");

        // find the property applicable
        final String propertyName = method.getName().replaceFirst("is|get|set", "");
        for(int i = 0; i < properties.size(); ++i){
            final Property p = properties.get(i);
            if( p.getName().equalsIgnoreCase(propertyName)){
                if( setter ){
                    properties.set(i, new Property(p.getName(), args[0]));
                }
                // TODO if this bean is immutable then clone (defensive copy) this object
                return p.getValue();
            }
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
                + " [Proxy (" + getClass().getSimpleName() + ")] w/ " + toString(properties);
    }

    // Package protected ---------------------------------------------

    BeanContextChild getBeanContextChild(){
        return contextChild;
    }

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------



    /** return true if any of the propertyDescriptors have a setter method.
     * also needs to ensure property's type is immutable too ?!
     **/
    private static boolean isImmutable(final Class<?> cls) throws IntrospectionException{

        // during development just return false
        return false;
//        final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(cls).getPropertyDescriptors();
//
//        boolean result = false;
//        for(PropertyDescriptor property : propertyDescriptors){
//            result |= null == property.getReadMethod();
//        }
//
//        return result;
    }

    // Inner classes -------------------------------------------------



}
