/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * DataModelTest.java
 *
 * Created on 30 January 2007, 15:33
 *
 */

package no.sesat.search.datamodel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import no.sesat.search.datamodel.generic.DataObject;
import org.apache.commons.beanutils.MappedPropertyDescriptor;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/** Assert some general rules the DataModel API must follow.
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class DataModelTest {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DataModelTest.class);

    private static final String ASSERT_METHOD_NOT_GETTER_OR_SETTER
            = " is not either a getter or setter to this javaBean. Properties are ";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------


    // Constructors --------------------------------------------------

    /** Creates a new instance of DataModelTest */
    public DataModelTest() {
    }

    // Public --------------------------------------------------------

    /** Ensure that all methods within the DataModel heirarchy are either getter or setters to properties. *
     * @throws java.lang.Exception
     */
    @Test
    public void testJavaBeanAPI() throws Exception{

        ensureJavaBeanAPI(DataModel.class);
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void ensureJavaBeanAPI(final Class<?> cls) throws IntrospectionException{

        LOG.info("ensuring pure JavaBean API on " + cls.getSimpleName());

        // collect the getter and setters
        final Collection<Method> propertyMethods = new ArrayList<Method>();

        collectProperties(cls, propertyMethods);

        final Collection<String> gettersSatisfied = new ArrayList<String>();
        final Collection<String> settersSatisfied = new ArrayList<String>();

        // now scan the methods
        for(Method method : cls.getMethods()){

            final boolean setter = method.getName().startsWith("set");
            final String propertyName = method.getName().replaceFirst("is|get|set", "");
            final Collection<String> propertiesSatisfied = setter ? settersSatisfied : gettersSatisfied;

            if( !propertiesSatisfied.contains(propertyName) ){
                LOG.info(" method -->   " + method.getName());

                assert propertyMethods.contains(method)
                        : method.toString() + ASSERT_METHOD_NOT_GETTER_OR_SETTER + propertyMethods;

                propertiesSatisfied.add(propertyName);
            }else{
                LOG.info(" method -->   " + method.getName() + " previously satisfied");
            }
        }

        LOG.info(cls.getSimpleName() + " passed API test");
    }

    private void collectProperties(
            final Class<?> cls,
            final Collection<Method> propertyMethods) throws IntrospectionException{

        final List<PropertyDescriptor> props
                = Arrays.asList(Introspector.getBeanInfo(cls).getPropertyDescriptors());
//                = new ArrayList<PropertyDescriptor>();
//        props.addAll(Arrays.asList(Introspector.getBeanInfo(cls, Introspector.IGNORE_ALL_BEANINFO).getPropertyDescriptors()));
//        Introspector.flushFromCaches(cls);
//        props.addAll(Arrays.asList(Introspector.getBeanInfo(cls, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors()));
//        Introspector.flushFromCaches(cls);

        for(PropertyDescriptor property : props){

            LOG.info(" property --> " + property.getName());

            handleProperty(propertyMethods, property);
            if(property instanceof MappedPropertyDescriptor){
                handleMappedProperty(propertyMethods, (MappedPropertyDescriptor)property);
            }
        }

        // repeat again on all implemented interfaces
        for(Class<?> c : cls.getInterfaces()){
            collectProperties(c, propertyMethods);
        }
    }

    private void handleProperty(
            final Collection<Method> propertyMethods,
            final PropertyDescriptor property) throws IntrospectionException{

        if( null != property.getReadMethod() ){
            propertyMethods.add(property.getReadMethod());
            // recurse down the datamodel heirarchy
            if(null != property.getPropertyType().getAnnotation(DataObject.class)){
                ensureJavaBeanAPI(property.getPropertyType());
            }
        }
        if( null != property.getWriteMethod() ){
            propertyMethods.add(property.getWriteMethod());
        }
    }

    private void handleMappedProperty(
            final Collection<Method> propertyMethods,
            final MappedPropertyDescriptor property) throws IntrospectionException{

        if( null != property.getMappedReadMethod() ){
            propertyMethods.add(property.getMappedReadMethod());
            // recurse down the datamodel heirarchy
            if(null != property.getMappedPropertyType().getAnnotation(DataObject.class)){
                ensureJavaBeanAPI(property.getMappedPropertyType());
            }
        }
        if( null != property.getMappedWriteMethod() ){
            propertyMethods.add(property.getMappedWriteMethod());
        }
    }

    // Inner classes -------------------------------------------------

}
