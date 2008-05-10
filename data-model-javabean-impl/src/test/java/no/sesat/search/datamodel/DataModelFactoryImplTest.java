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
 * DataModelFactoryImplTest.java
 *
 * Created on 30 January 2007, 15:33
 *
 */

package no.sesat.search.datamodel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import no.sesat.search.datamodel.generic.DataNode;
import no.sesat.search.datamodel.generic.DataObject;
import no.sesat.search.datamodel.generic.DataObject.Property;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/** Instantiate all the nodes and objects that exist in the datamodel.
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class DataModelFactoryImplTest {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DataModelFactoryImplTest.class);

    private static final String ASSERT_METHOD_NOT_GETTER_OR_SETTER
            = " is not either a getter or setter to this javaBean. Properties are ";

    // Attributes ----------------------------------------------------

    private final DataModelFactory factory;
    private final DataModel datamodel;

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of DataModelTest */
    public DataModelFactoryImplTest() {
        factory = new DataModelFactoryImpl(new DataModelFactory.Context(){
            public Site getSite() {
                return Site.DEFAULT;
            }

            public PropertiesLoader newPropertiesLoader(final SiteContext siteCxt,
                                                        final String resource,
                                                        final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
            }
        });
        datamodel = factory.instantiate();
    }

    // Public --------------------------------------------------------

    /**  **/
    @Test
    public void testInstantiateDataObjects() throws Exception{

        LOG.info("testInstantiateDataObjects()");
        scan(DataObject.class, DataModel.class,
            new Command(){
                public void execute(Object... args) {
                    try{
                        final Class<?> cls = (Class<?>)args[0];
                        testInstantiate(cls);
                    }catch(IntrospectionException ie){
                        LOG.info(ie.getMessage(), ie);
                        throw new RuntimeException(ie.getMessage(), ie);
                    }
                }
        });
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testInstantiateDataNodes() throws Exception{

        LOG.info("testInstantiateDataNodes()");
        scan(DataNode.class, DataModel.class,
            new Command(){
                public void execute(Object... args) {
                    try{
                        final Class<?> cls = (Class<?>)args[0];
                        testInstantiate(cls);
                    }catch(IntrospectionException ie){
                        LOG.info(ie.getMessage(), ie);
                        throw new RuntimeException(ie.getMessage(), ie);
                    }
                }
        });
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testDataObjectGetters() throws Exception{

        LOG.info("testDataObjectGetters()");
        scan(DataObject.class, DataModel.class,
            new Command(){
                public void execute(Object... args) {

                    try{
                        final Class<?> cls = (Class<?>)args[0];
                        final Object dataObject = testInstantiate(cls);

                        final PropertyDescriptor[] properties = Introspector.getBeanInfo(cls).getPropertyDescriptors();
                        for(PropertyDescriptor property : properties){
                            if( null != property.getReadMethod() ){
                                final Object value = invoke(property.getReadMethod(), dataObject, new Object[0]);
                                LOG.info("        Getter on " + property.getName() + " returned " + value);
                            }
                            if( property instanceof MappedPropertyDescriptor ){
                                final MappedPropertyDescriptor mappedProperty = (MappedPropertyDescriptor)property;
                                if(null != mappedProperty.getReadMethod()){
                                    final Object value
                                            = invoke(mappedProperty.getMappedReadMethod(), dataObject, "");
                                    LOG.info("        Getter on " + mappedProperty.getName() + " returned " + value);
                                }
                            }
                        }
                    }catch(IntrospectionException ie){
                        LOG.info(ie.getMessage(), ie);
                        throw new RuntimeException(ie.getMessage(), ie);
                    }
                }
        });
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private <T> T testInstantiate(final Class<T> cls) throws IntrospectionException{

       LOG.info("    instantiating " + cls.getSimpleName());


       final PropertyDescriptor[] properties = Introspector.getBeanInfo(cls).getPropertyDescriptors();
       final Property[] props = new Property[properties.length];
       for(int i = 0; i < props.length; ++i){
           props[i] = new Property(properties[i].getName(), null);
       }

       final T data = factory.instantiate(cls, datamodel, props);

       assert null != data : "instantiate(" + cls.getSimpleName() + ", properties) returned null";
       LOG.info("      instantiated .."
               + data.toString().replaceFirst("no.sesat.search.datamodel", ""));

       return data;
    }

    private void scan(
            final Class<? extends Annotation> type,
            final Class<?> cls,
            final Command command) throws IntrospectionException{

        LOG.info("scanning " + cls.getSimpleName());
        final PropertyDescriptor[] properties = Introspector.getBeanInfo(cls).getPropertyDescriptors();
        for(PropertyDescriptor property : properties){


            final Class<?> propCls = property instanceof MappedPropertyDescriptor
                    ? ((MappedPropertyDescriptor)property).getMappedPropertyType()
                    : property.getPropertyType();

            LOG.info("  checking property " + property.getName() + " [" + propCls.getSimpleName() + ']');

            if(null != propCls.getAnnotation(type)){
                command.execute(propCls);
            }

            if(null != propCls.getAnnotation(DataNode.class)){
                // also descend down dataNodes in the datamodel
                scan(type, propCls, command);
            }
        }

        // repeat again on all implemented interfaces
        for(Class<?> c : cls.getInterfaces()){
            scan(type, c, command);
        }
    }


    /** Calls the method.invoke(..) wrapping any thrown exceptions with a RuntimeException. **/
    private Object invoke(final Method method, final Object dataObject, final Object... args){

        try{
            return method.invoke(dataObject, args);

        }catch(IllegalAccessException iae){
            LOG.error(iae.getMessage(), iae);
            throw new RuntimeException(iae.getMessage(), iae);
        }catch(IllegalArgumentException iae){
            LOG.info(iae.getMessage(), iae);
            throw new RuntimeException(iae.getMessage(), iae);
        }catch(InvocationTargetException ite){
            LOG.info(ite.getMessage(), ite);
            throw new RuntimeException(ite.getMessage(), ite);
        }
    }

    // Inner classes -------------------------------------------------

    private interface Command{
        void execute(Object... args);
    }
}
