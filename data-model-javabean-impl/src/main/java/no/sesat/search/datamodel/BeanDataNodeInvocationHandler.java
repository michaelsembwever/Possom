/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * BeanDataNodeInvocationHandler.java
 *
 * Created on 23 January 2007, 21:34
 *
 */

package no.sesat.search.datamodel;

import no.sesat.search.datamodel.generic.DataNode;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.DataObject.Property;
import no.sesat.search.datamodel.generic.MapDataObject;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
class BeanDataNodeInvocationHandler<T> extends BeanDataObjectInvocationHandler<T> {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(BeanDataNodeInvocationHandler.class);

    // Attributes ----------------------------------------------------

    private final BeanDataObjectInvocationHandler<T> dataObject;

    // Static --------------------------------------------------------

    static <T> BeanDataNodeInvocationHandler<T> instanceOf(final Class<T> cls, final Property... properties)
            throws IntrospectionException {

        return new BeanDataNodeInvocationHandler<T>(cls, new PropertyInitialisor(cls, properties));
    }

    // Constructors --------------------------------------------------

    /** Creates a new instance of ProxyBeanDataObject */
    private BeanDataNodeInvocationHandler(
            final Class<T> cls,
            final PropertyInitialisor properties)
                throws IntrospectionException {
        
        this(cls, new BeanContextSupport(), properties);
    }
    
    /**
     * Creates a new instance of ProxyBeanDataObject
     */
    protected BeanDataNodeInvocationHandler(
            final Class<T> cls,
            final BeanContext context,
            final PropertyInitialisor properties) throws IntrospectionException {

        super(cls, context, properties.properties);

        // make context to contextChild bindings
        for (PropertyDescriptor property : properties.childPropertyDescriptors) {
            for (Property p : properties.allProperties) {
                if (p.getName().equals(property.getName())) {
                    if (p.getValue() instanceof MapDataObject) {
                        for (Object obj : ((MapDataObject) p.getValue()).getValues().values()) {
                            addChild(obj);
                        }
                    } else {
                        addChild(p.getValue());
                    }
                    break;
                }
            }
        }

        // delegate our own properties
        dataObject = new BeanDataObjectInvocationHandler(cls, properties.delegatedProperties);
    }

    // Public --------------------------------------------------------

    @Override
    public Object invoke(final Object obj, final Method method, final Object[] args) throws Throwable {

        assureAccessAllowed(method);
        
        // try our dataObject|dataNode delegated-properties
        try {
            return super.invoke(obj, method, args);

        } catch (IllegalArgumentException iae) {
            LOG.debug("property not one of our own. " + iae.getMessage());
        }

        // try non-(dataObject|dataNode) delegated-properties
        try {
            return dataObject.invoke(obj, method, args);

        } catch (IllegalArgumentException iae) {
            LOG.debug("property not from delegate", iae);
        }

        // try pure self methods
        try {
            return method.invoke(this, args);

        } catch (IllegalAccessException iae) {
            LOG.info(iae.getMessage(), iae);
        } catch (IllegalArgumentException iae) {
            LOG.info(iae.getMessage(), iae);
        } catch (InvocationTargetException ite) {
            LOG.info(ite.getMessage(), ite);
        }

        throw new IllegalArgumentException("Method to invoke doesn't map to bean property");
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    /**
     * obj may be null. 
     */
    @Override
    protected void addChild(final Object obj) {

        if (null != obj) {

            assert isDataObjectOrNode(obj) : "my own properties should only be Data(Object|Node)s";
            assert isSerializable(obj) : "Object not serializable: " + obj;
            final BeanDataObjectInvocationHandler<?> childsNewHandler
                    = (BeanDataObjectInvocationHandler<?>) Proxy.getInvocationHandler(obj);
            context.add(childsNewHandler.getBeanContextChild());

        }
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
            /* Do nothing, return value already correct */
        } catch (IOException e) {
            /* Do nothing, return value already correct */
        }
        return correct;
    }
    
    /**
     * obj may be null. 
     */
    @Override
    protected void removeChild(final Object obj) {

        if (null != obj) {

            assert isDataObjectOrNode(obj) : "my own properties should only be Data(Object|Node)s";

            final BeanDataObjectInvocationHandler<?> childsOldHandler
                    = (BeanDataObjectInvocationHandler<?>) Proxy.getInvocationHandler(obj);
            context.remove(childsOldHandler.getBeanContextChild());

        }
    }
    
    // Private -------------------------------------------------------

    private boolean isDataObjectOrNode(final Object obj){
        
        boolean dataObjectOrNode = false;
        final Class[] interfaces = obj.getClass().getInterfaces();
        for(int i = 0; !dataObjectOrNode && i < interfaces.length; ++i){
            dataObjectOrNode = null != interfaces[i].getAnnotation(DataObject.class);
            dataObjectOrNode |= null != interfaces[i].getAnnotation(DataNode.class);
        }
        return dataObjectOrNode;
    }

    // Inner classes -------------------------------------------------

    static final class PropertyInitialisor<T> {

        final Property[] allProperties;
        final Property[] properties;
        final Property[] delegatedProperties;
        final PropertyDescriptor[] childPropertyDescriptors;

        PropertyInitialisor(
                final Class<T> cls,
                final Property... allProperties) throws IntrospectionException {

            this.allProperties = allProperties;
            final List<Property> properties = new ArrayList<Property>();
            final List<Property> delegatedProperties = new ArrayList<Property>();
            final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();

            final PropertyDescriptor[] allPropertyDescriptors = Introspector.getBeanInfo(cls).getPropertyDescriptors();
            // split properties between children dataObjects and our own properties
            final List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();

            for (int i = 0; i < allPropertyDescriptors.length; ++i) {

                final PropertyDescriptor property = allPropertyDescriptors[i];

                final Class<?> propCls;
                if (property instanceof MappedPropertyDescriptor) {
                    propCls = ((MappedPropertyDescriptor) property).getMappedPropertyType();
                    ++i; // the next propertyDescriptor is the synonym to this mappedPropertyDescriptor
                } else {
                    propCls = property.getPropertyType();
                }

                // FIXME the following if-else only deals with normals properties (not mapped).
                if (null != propCls.getAnnotation(DataObject.class) || null != propCls.getAnnotation(DataNode.class)) {

                    descriptors.add(property);
                    for (Property p : allProperties) {
                        final String name = p.getName();
                        if (name.equals(property.getName()) || name.equals(allPropertyDescriptors[i].getName())) {
                            if (property instanceof MappedPropertyDescriptor) {
                                // mappedPropertyDescriptor original & synonym
                                properties.add(new Property(property.getName(), p.getValue()));
                                properties.add(new Property(allPropertyDescriptors[i].getName(), p.getValue()));
                            } else {
                                properties.add(p);
                            }
                            break;
                        }
                    }

                } else {
                    propertyDescriptors.add(property);
                    for (Property p : allProperties) {
                        if (p.getName().equals(property.getName())) {
                            delegatedProperties.add(p);
                            break;
                        }
                    }
                }
            }

            this.properties = properties.toArray(new Property[properties.size()]);
            this.delegatedProperties = delegatedProperties.toArray(new Property[delegatedProperties.size()]);
            this.childPropertyDescriptors = descriptors.toArray(new PropertyDescriptor[descriptors.size()]);
        }
    }
}
