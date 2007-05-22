// Copyright (2007) Schibsted Søk AS
/*
 * BeanDataNodeInvocationHandler.java
 *
 * Created on 23 January 2007, 21:34
 *
 */

package no.schibstedsok.searchportal.datamodel;

import no.schibstedsok.searchportal.datamodel.generic.DataNode;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.generic.DataObject.Property;
import no.schibstedsok.searchportal.datamodel.generic.MapDataObject;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextSupport;
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
    private final BeanContext context = new BeanContextSupport();

    // Static --------------------------------------------------------

    static <T> BeanDataNodeInvocationHandler<T> instanceOf(final Class<T> cls, final Property... properties)
            throws IntrospectionException {

        return new BeanDataNodeInvocationHandler<T>(cls, new PropertyInitialisor(cls, properties));
    }

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of ProxyBeanDataObject
     */
    protected BeanDataNodeInvocationHandler(
            final Class<T> cls,
            final PropertyInitialisor properties) throws IntrospectionException {

        super(cls, properties.properties);

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

        // try our dataObject|dataNode delegated-properties
        try {
            return super.invoke(obj, method, args);

        } catch (IllegalArgumentException iae) {
            LOG.debug("property not one of our own", iae);
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

    @Override
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

    // Private -------------------------------------------------------


    /**
     * obj may be null. *
     */
    private void addChild(final Object obj) {

        if (null != obj) {

            assert null != obj.getClass().getAnnotation(DataObject.class)
                    || null != obj.getClass().getAnnotation(DataNode.class)
                    : "my own properties should only be Data(Object|Node)s";

            final BeanDataObjectInvocationHandler<?> childsNewHandler
                    = (BeanDataObjectInvocationHandler<?>) Proxy.getInvocationHandler(obj);
            context.add(childsNewHandler.getBeanContextChild());

        }
    }

    /**
     * obj may be null. *
     */
    private void removeChild(final Object obj) {

        if (null != obj) {

            assert null != obj.getClass().getAnnotation(DataObject.class)
                    || null != obj.getClass().getAnnotation(DataNode.class)
                    : "my own properties should only be Data(Object|Node)s";

            final BeanDataObjectInvocationHandler<?> childsOldHandler
                    = (BeanDataObjectInvocationHandler<?>) Proxy.getInvocationHandler(obj);
            context.remove(childsOldHandler.getBeanContextChild());

        }
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
