/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * BeanDataObjectInvocationHandler.java
 *
 * Created on 23 January 2007, 21:34
 *
 */

package no.sesat.search.datamodel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.sesat.search.datamodel.BeanDataModelInvocationHandler.DataModelBeanContextSupport;
import no.sesat.search.datamodel.access.AccessAllow;
import no.sesat.search.datamodel.access.AccessDisallow;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.datamodel.access.DataModelAccessException;
import no.sesat.search.datamodel.generic.DataObject.Property;
import no.sesat.search.datamodel.generic.MapDataObject;
import no.sesat.search.datamodel.generic.MapDataObjectSupport;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.generic.StringDataObjectSupport;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
class BeanDataObjectInvocationHandler<T> implements InvocationHandler, Serializable {

    // Constants -----------------------------------------------------

    private static final Map<Property[], WeakReference<BeanDataObjectInvocationHandler<?>>> instances
            = new HashMap<Property[], WeakReference<BeanDataObjectInvocationHandler<?>>>();

    private static final ReentrantReadWriteLock instancesLock = new ReentrantReadWriteLock();

    private static final Logger LOG = Logger.getLogger(BeanDataObjectInvocationHandler.class);
          
    private static final boolean ACCESS_CONTROLLED = !Boolean.getBoolean("sesat.datamodel.accesscontrol.ignore");

    // Attributes ----------------------------------------------------


    private Class<T> implementOf;
    private Object support;
    private boolean immutable;

    // properties: the only part of this class that be immutable and reused
    protected List<Property> properties = new CopyOnWriteArrayList<Property>();

    protected BeanContext context;

    transient private Map<Method,InvocationTarget> invocationTargetCache = new ConcurrentHashMap<Method,InvocationTarget>();
    transient private Map<Method,Method> supportMethodCache = new ConcurrentHashMap<Method,Method>();

    // Static --------------------------------------------------------

    @SuppressWarnings("unchecked")
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
                    instances.put(properties, new WeakReference<BeanDataObjectInvocationHandler<?>>(instance));
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

    /** No-arg constructor to support serialization */
    protected BeanDataObjectInvocationHandler() {
        implementOf = null;
        context = new BeanContextSupport();
        support = new Object();
        immutable = false;
    };
   
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        invocationTargetCache = new ConcurrentHashMap<Method,InvocationTarget>();
        supportMethodCache = new ConcurrentHashMap<Method,Method>();
        implementOf = (Class<T>) stream.readObject();
        context = (BeanContext) stream.readObject();
        support = stream.readObject();
        immutable = stream.readBoolean();
        properties = (List<Property>) stream.readObject();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(implementOf);
        stream.writeObject(context);
        stream.writeObject(support);
        stream.writeBoolean(immutable);
        stream.writeObject(properties);
    }
    
    /** Creates a new instance of ProxyBeanDataObject */
    protected BeanDataObjectInvocationHandler(
            final Class<T> cls,
            final Property... properties)
                throws IntrospectionException {
        
        this(cls, new BeanContextSupport(), properties);
    }

    /** Creates a new instance of ProxyBeanDataObject */
    @SuppressWarnings("unchecked")
    protected BeanDataObjectInvocationHandler(
            final Class<T> cls,
            final BeanContext context,
            final Property... properties)
                throws IntrospectionException {

        implementOf = cls;
        this.context = context;
        
        final List<Property> propertiesLeftToAdd = new ArrayList<Property>(Arrays.asList(properties));

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
            assert checkPropertyClass(implementOf, p) : "Property '" + p.getName() + "' not of right class";
            assert isSerializable(p.getValue()) : "Property value not serializable: " + p.getName() + " " + p.getValue();
            addProperty(p);
        }

        this.immutable = isImmutable(cls);
    }   
    
    private boolean isSerializable(final Object obj) {
        boolean correct = false;
        if (obj == null) {
            return true;
        }
        
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream os = new ObjectOutputStream(baos);
            
            os.writeObject(obj);
            correct = true;
        } catch (NotSerializableException e) {
            /* Do nothing, return value already set */
        } catch (IOException e) {
            /* Do nothing, return value already set */
        }
        return correct;
    }
    
    private boolean checkPropertyClass(final Class cls, final Property property) {
        boolean correct = false;
        if (property.getValue() == null) {
            return true;
        }
        
        try {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(cls).getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                if (descriptor.getName().equals(property.getName())) {
                    final Class<?> propertyType = property.getValue().getClass().equals(MapDataObjectSupport.class) 
                            ? Map.class : property.getValue().getClass();
                    correct |= descriptor.getPropertyType() == null || descriptor.getPropertyType().isAssignableFrom(propertyType);
                    break; 
                }
            }
        } catch (IntrospectionException e) {
            /* Do nothing, return value already set */
        }
        return correct;
    }
    
    // Public --------------------------------------------------------

    /** {@inherit} **/
    public Object invoke(final Object obj, final Method method, final Object[] args) throws Throwable {
        
        assureAccessAllowed(method);

        final boolean setter = method.getName().startsWith("set");
        final String propertyName = method.getName().replaceFirst("is|get|set", "");
        final InvocationTarget invocationTarget = invocationTargetCache.get(method);

        if(InvocationTarget.SUPPORT == invocationTarget || null == invocationTarget){

            final Method invokeSupportMethod = null != supportMethodCache.get(method)
                    ? supportMethodCache.get(method)
                    : findSupport(propertyName, setter);

            if(null != invokeSupportMethod){
                if(null == invocationTarget){

                    invocationTargetCache.put(method, InvocationTarget.SUPPORT);
                    supportMethodCache.put(method, invokeSupportMethod);
                }
                return invokeSupport(invokeSupportMethod, support, args);
            }
        }

        if(InvocationTarget.PROPERTY == invocationTarget || null == invocationTarget){

            final Property invokePropertyResult = invokeProperty(propertyName, setter, args);

            if(null != invokePropertyResult){
                if(null == invocationTarget){
                    invocationTargetCache.put(method, InvocationTarget.PROPERTY);
                }
                return invokePropertyResult.getValue() instanceof MapDataObject
                            && Map.class.isAssignableFrom(method.getReturnType())
                        ? ((MapDataObject)invokePropertyResult.getValue()).getValues()
                        : invokePropertyResult.getValue();
            }
        }

        if(InvocationTarget.SELF == invocationTarget || null == invocationTarget){

            final Object invokeSelfResult = invokeSelf(method, args);

            if(null != invokeSelfResult){
                if(null == invocationTarget){
                    invocationTargetCache.put(method, InvocationTarget.SELF);
                }
                return invokeSelfResult;
            }
        }


        throw new IllegalArgumentException("Method to invoke is not a getter or setter to any bean property: " 
                + method.getName());

    }

    @Override
    public String toString(){
        return implementOf.getSimpleName()
                + " [Proxy (" + getClass().getSimpleName() + ")] w/ " + toString(properties);
    }

    // Package protected ---------------------------------------------

    BeanContextChild getBeanContextChild(){
        return context;
    }

    // Protected -----------------------------------------------------

    protected final void assureAccessAllowed(final Method method) throws IllegalAccessException{
        
        // we need the current ControlLevel
        BeanContext beanContext = context;
        while(null != beanContext.getBeanContext()){
            beanContext = beanContext.getBeanContext();
        }
        if(beanContext instanceof DataModelBeanContextSupport){
            final ControlLevel level = ((DataModelBeanContextSupport)beanContext).getControlLevel();
            final AccessAllow allow = method.getAnnotation(AccessAllow.class);
            final AccessDisallow disallow = method.getAnnotation(AccessDisallow.class);
            
            LOG.trace("level " + level);
            LOG.trace("method " + method);
            LOG.trace("allow " + allow);
            LOG.trace("disallow " + disallow);
            
            boolean allowed = false;
            boolean disallowed = false;
            if(null != allow){
                for(ControlLevel cl : allow.value()){
                    allowed |= cl == level;
                }
            }
            if(null != disallow){
                for(ControlLevel cl : disallow.value()){
                    disallowed |= cl == level;
                }
            }
            if(ACCESS_CONTROLLED && ((null != allow && !allowed) || (null != disallow && disallowed))){
                throw new DataModelAccessException(method, level);
            }
        }
    }

    protected Object invokeSupport(final Method m, final Object support, final Object[] args){

        Object result = null;
        try{

//                    if( !support.getClass().isAssignableFrom(m.getDeclaringClass()) ){
//                        // try to find method again since m is an override and won't be found as is in support
//                        m = support.getClass().getMethod(m.getName(), m.getParameterTypes());
//                    }

            result = m.invoke(support, args);

        }catch(IllegalAccessException iae){
            LOG.info(iae.getMessage(), iae);
        }catch(IllegalArgumentException iae){
            LOG.trace(iae.getMessage());
//                }catch(NoSuchMethodException nsme){
//                    LOG.trace(nsme.getMessage());
        }catch(InvocationTargetException ite){
            LOG.info(ite.getMessage(), ite);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected Property invokeProperty(final String propertyName, final boolean setter, final Object[] args) {

        Property result = null;
        for (int i = 0; i < properties.size(); ++i) {
            final Property p = properties.get(i);
            if (p.getName().equalsIgnoreCase(propertyName)) {
                if (setter) {

                    // set the new child dataObject
                    if (p.getValue() instanceof MapDataObject && args.length > 1) {

                        final MapDataObject mpd = (MapDataObject) p.getValue();
                        // detach the old contextChild
                        removeChild(mpd.getValue((String) args[0]));
                        // update property
                        mpd.setValue((String) args[0], args[1]);
                        // add the new contextChild
                        addChild(args[1]);

                    } else {

                        // detach the old contextChild
                        removeChild(p.getValue());
                        // update property
                        properties.set(i, new Property(p.getName(), args[0]));
                        // add the new contextChild
                        addChild(args[0]);
                    }

                }
                result = null != p && null != args && p.getValue() instanceof MapDataObject && args.length > (setter ? 1 : 0)
                        ? new Property((String) args[0], ((MapDataObject) p.getValue()).getValue((String) args[0]))
                        : p;
                break;
            }
        }
        return result;
    }

    
    protected Object invokeSelf(final Method method, final Object[] args){

        // try invoking one of our own methods. (Works for example on methods declared by the Object class).

        Object result = null;
        try{
            result = method.invoke(this, args);

        }catch(IllegalAccessException iae){
            LOG.info(iae.getMessage(), iae);
        }catch(IllegalArgumentException iae){
            LOG.debug(iae.getMessage());
        }catch(InvocationTargetException ite){
            LOG.info(ite.getMessage(), ite);
        }
        return result;
    }
    
    /**
     * obj may be null. 
     */
    protected void addChild(final Object obj) {
        // does nothing. DataObject don't have children.
    }

    /**
     * obj may be null. 
     */
    protected void removeChild(final Object obj) {
        // does nothing. DataObject don't have children.
    }    

    // Private -------------------------------------------------------

    private Method findSupport(final String propertyName, final boolean setter) throws IntrospectionException{

        // If there's a support instance, use it first.

        Method m = null;
        if( null != support ){

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
        }
        return m;
    }


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


    private enum InvocationTarget{
        PROPERTY,
        SELF,
        SUPPORT;
    }

}
