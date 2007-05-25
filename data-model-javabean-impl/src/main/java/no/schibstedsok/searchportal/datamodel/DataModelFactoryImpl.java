// Copyright (2007) Schibsted Søk AS
/*
 * DataModelFactoryImpl.java
 *
 * Created on 27 January 2007, 22:49
 *
 */

package no.schibstedsok.searchportal.datamodel;


import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import no.schibstedsok.searchportal.datamodel.access.ControlLevel;
import no.schibstedsok.searchportal.datamodel.generic.DataNode;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.generic.DataObject.Property;
import no.schibstedsok.searchportal.datamodel.generic.MapDataObjectSupport;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;

/** Default implementation of the DataModelFactory.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
final class DataModelFactoryImpl extends DataModelFactory{

    // Constants -----------------------------------------------------


    private static final Logger LOG = Logger.getLogger(DataModelFactoryImpl.class);

    private static final String ERR_ONLY_DATA_NODE_OR_OBJECT
            = "DataModelFactory can only instantiate DataObject or DataNode classes";
    private static final String ERR_ONLY_JAVA_BEAN_DATA_OBJECT
            = "DataModelFactory can only instantiate DataObjects following a JavaBean pattern";

    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    DataModelFactoryImpl(final Context cxt){
        super(cxt);
    }

    // Public --------------------------------------------------------

    public DataModel instantiate() {

        try{
            final Class<DataModel> cls = DataModel.class;
            
            final PropertyDescriptor[] propDescriptors 
                    = Introspector.getBeanInfo(DataModel.class).getPropertyDescriptors();
            final Property[] properties = new Property[propDescriptors.length];
            for(int i = 0; i < properties.length; ++i){
                properties[i] = new Property(
                        propDescriptors[i].getName(), 
                        propDescriptors[i] instanceof MappedPropertyDescriptor
                        ? new MapDataObjectSupport(Collections.EMPTY_MAP)
                        : null);
            }
            
            final InvocationHandler handler = new BeanDataModelInvocationHandler(
                    new BeanDataModelInvocationHandler.PropertyInitialisor(DataModel.class, properties));
            
            return (DataModel) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, handler);
            
        }catch(IntrospectionException ie){
            throw new IllegalStateException("Need to introspect DataModel properties before instantiation");
        }
    }

    public <T> T instantiate(final Class<T> cls, final Property... properties) {

        try{
            final InvocationHandler handler;

            if(null != cls.getAnnotation(DataNode.class)){
                handler = BeanDataNodeInvocationHandler.instanceOf(cls, properties);

            }else if(null != cls.getAnnotation(DataObject.class)){
                handler = BeanDataObjectInvocationHandler.instanceOf(cls, properties);

            }else{
                throw new IllegalArgumentException(ERR_ONLY_DATA_NODE_OR_OBJECT);
            }

            return (T)Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, handler);

        }catch(IntrospectionException ie){
            throw new IllegalArgumentException(ERR_ONLY_JAVA_BEAN_DATA_OBJECT);
        }
    }

    public DataModel assignControlLevel(final DataModel datamodel, final ControlLevel controlLevel){

        final BeanDataModelInvocationHandler handler 
                = (BeanDataModelInvocationHandler) Proxy.getInvocationHandler(datamodel);
        
        handler.setControlLevel(controlLevel);
        
        return datamodel;
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
