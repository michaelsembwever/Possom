/*
 * DataModelFactoryImplTest.java
 *
 * Created on 30 January 2007, 15:33
 *
 */

package no.schibstedsok.searchportal.datamodel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import no.schibstedsok.searchportal.datamodel.generic.DataNode;
import no.schibstedsok.searchportal.datamodel.generic.DataObject;
import no.schibstedsok.searchportal.datamodel.generic.DataObject.Property;
import no.schibstedsok.searchportal.site.Site;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/** Instantiate all the nodes and objects that exist in the datamodel.
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class DataModelFactoryImplTest {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DataModelFactoryImplTest.class);

    private static final String ASSERT_METHOD_NOT_GETTER_OR_SETTER
            = " is not either a getter or setter to this javaBean. Properties are ";

    // Attributes ----------------------------------------------------

    private final DataModelFactory factory;

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of DataModelTest */
    public DataModelFactoryImplTest() {
        factory = new DataModelFactoryImpl(new DataModelFactory.Context(){
            public Site getSite() {
                return Site.DEFAULT;
            }
        });
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

       final T data = factory.instantiate(cls, props);

       assert null != data : "instantiate(" + cls.getSimpleName() + ", properties) returned null";
       LOG.info("      instantiated .."
               + data.toString().replaceFirst("no.schibstedsok.searchportal.datamodel", ""));

       return data;
    }

    private void scan(
            final Class<? extends Annotation> type,
            final Class<?> cls,
            final Command command) throws IntrospectionException{

        LOG.info("scanning " + cls.getSimpleName());
        final PropertyDescriptor[] properties = Introspector.getBeanInfo(cls).getPropertyDescriptors();
        for(PropertyDescriptor property : properties){

            LOG.info("  checking property " + property.getName()
                    + " [" + property.getPropertyType().getSimpleName() + ']');

            if(null != property.getPropertyType().getAnnotation(type)){
                command.execute(property.getPropertyType());
            }
            if(property instanceof MappedPropertyDescriptor){
                final MappedPropertyDescriptor mappedProperty = (MappedPropertyDescriptor)property;
                if(null != mappedProperty.getMappedPropertyType().getAnnotation(type)){
                    command.execute(property.getPropertyType());
                }
            }
            if(null != property.getPropertyType().getAnnotation(DataNode.class)){
                // also descend down dataNodes inthe datamodel
                scan(type, property.getPropertyType(), command);
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
