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
 *
 * DataModelFactoryImpl.java
 *
 * Created on 27 January 2007, 22:49
 *
 */

package no.sesat.search.datamodel;


import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import no.sesat.commons.reflect.ConcurrentProxy;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.datamodel.generic.DataNode;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.DataObject.Property;
import no.sesat.search.datamodel.generic.MapDataObjectSupport;
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

    @SuppressWarnings("unchecked")
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
            
            return (DataModel) ConcurrentProxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, handler);
            
        }catch(IntrospectionException ie){
            throw new IllegalStateException("Need to introspect DataModel properties before instantiation");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T instantiate(final Class<T> cls, final DataModel datamodel, final Property... properties) {

        try{
            final InvocationHandler handler;

            if(null != cls.getAnnotation(DataNode.class)){
                handler = BeanDataNodeInvocationHandler.instanceOf(cls, datamodel, properties);

            }else if(null != cls.getAnnotation(DataObject.class)){
                handler = BeanDataObjectInvocationHandler.instanceOf(cls, properties);

            }else{
                throw new IllegalArgumentException(ERR_ONLY_DATA_NODE_OR_OBJECT);
            }

            return (T)ConcurrentProxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, handler);

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
    
    public ControlLevel currentControlLevel(final DataModel datamodel){
        
        final BeanDataModelInvocationHandler handler 
                = (BeanDataModelInvocationHandler) Proxy.getInvocationHandler(datamodel);
        
        return ((BeanDataModelInvocationHandler.DataModelBeanContextSupport)handler.context).getControlLevel();
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

}
