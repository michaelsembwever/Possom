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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.schibstedsok.searchportal.datamodel.generic.DataObject.Property;
import no.schibstedsok.searchportal.datamodel.generic.MapDataObject;
import no.schibstedsok.searchportal.datamodel.generic.MapDataObjectSupport;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObjectSupport;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
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
    private final Object support;
    private final boolean immutable;
    
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
        
        final List<Property> propertiesLeftToAdd = new ArrayList(Arrays.asList(properties));
        
        if( StringDataObject.class.isAssignableFrom(implementOf) ){
            
            String value = null;
            boolean found = false;
            for(Property p : properties){

                if("string".equals(p.getName())){
                    value = (String)p.getValue();
                    propertiesLeftToAdd.remove(p);
                    found = true;
                    break;
                }
            }
            
            support = found ? new StringDataObjectSupport(value) : null;
            
        }else if( MapDataObject.class.isAssignableFrom(implementOf)){
            
            Map<?,?> map = null;
            boolean found = false;
            for(Property p : properties){

                if("values".equals(p.getName())){
                    map = (Map<?,?>)p.getValue();
                    propertiesLeftToAdd.remove(p);
                    found = true;
                    break;
                }
            }
            
            support = found ? new MapDataObjectSupport(map) : null;
            
        }else{
            support = null;
        }
        
        for(Property p : propertiesLeftToAdd){
            addProperty(p);
        }
            
        this.immutable = isImmutable(cls);
    }



    // Public --------------------------------------------------------

    public Object invoke(final Object obj, final Method method, final Object[] args) throws Throwable {
        
        final boolean setter = method.getName().startsWith("set");
        final String propertyName = method.getName().replaceFirst("is|get|set", "");
        
        // If there's a support instance, use it first.
        if( null != support ){

            Method m = null;
            final PropertyDescriptor[] propDescriptors 
                    = Introspector.getBeanInfo(support.getClass().getInterfaces()[0]).getPropertyDescriptors();
            for( PropertyDescriptor pd : propDescriptors ){
                if( propertyName.equalsIgnoreCase(pd.getName()) ){
                    if(pd instanceof MappedPropertyDescriptor ){
                        final MappedPropertyDescriptor mpd = (MappedPropertyDescriptor)pd;
                        m = setter ? mpd.getMappedWriteMethod() : mpd.getMappedReadMethod();
                    }else{
                        m = setter ? pd.getWriteMethod() : pd.getReadMethod();
                    } 
                    break;
                }
            }
            if( null != m ){
                try{
                    
//                    if( !support.getClass().isAssignableFrom(m.getDeclaringClass()) ){
//                        // try to find method again since m is an override and won't be found as is in support
//                        m = support.getClass().getMethod(m.getName(), m.getParameterTypes());
//                    }
                    
                    return m.invoke(support, args);

                }catch(IllegalAccessException iae){
                    LOG.info(iae.getMessage(), iae);
                }catch(IllegalArgumentException iae){
                    LOG.trace(iae.getMessage());
//                }catch(NoSuchMethodException nsme){
//                    LOG.trace(nsme.getMessage());
                }catch(InvocationTargetException ite){
                    LOG.info(ite.getMessage(), ite);
                }
            }
        }
        
        
        // Try finding something out of our own map of bean properties.
        for(int i = 0; i < properties.size(); ++i){
            final Property p = properties.get(i);
            if( p.getName().equalsIgnoreCase(propertyName)){
                if( setter ){
                    properties.set(i, new Property(p.getName(), args[0]));

                }
                // TODO if this bean is immutable then return a clone (defensive copy) this object
                return p.getValue();
            }
        }

        // try invoking one of our own methods. (Works for example on methods declared by the Object class).
        try{
            return method.invoke(this, args);

        }catch(IllegalAccessException iae){
            LOG.info(iae.getMessage(), iae);
        }catch(IllegalArgumentException iae){
            LOG.info(iae.getMessage(), iae);
        }catch(InvocationTargetException ite){
            LOG.info(ite.getMessage(), ite);
        }

        throw new IllegalArgumentException("Method to invoke is not a getter or setter to any bean property");

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

    private boolean addProperty(final Property property){

        // clone it, so caller cannot alter value later
        return this.properties.add(new Property(property.getName(), property.getValue()));
    }

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
