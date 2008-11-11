/**
 * Copyright (2008) Schibsted Søk AS
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

package no.sesat.search.mode.config;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ModesSearchConfigurationDeserializer {
    private static final Logger LOG = Logger.getLogger(ModesSearchConfigurationDeserializer.class);

    public static void readSearchConfiguration(final SearchConfiguration.ModesW3cDomDeserialiser config,
            final Element element, final SearchConfiguration inherit) {
        HashMap<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();
        String[] path = new String[]{"no.sesat.search.mode.config"};
        Introspector.setBeanInfoSearchPath(path);
        try {
            BeanInfo info = Introspector.getBeanInfo(config.getClass());
            for (PropertyDescriptor d : info.getPropertyDescriptors()) {
                descriptors.put(d.getName(), d);
            }
        } catch (IntrospectionException e) {
            LOG.error("Failed to get bean info from class " + config.getClass().getSimpleName(), e);
        }

        NamedNodeMap attribs = element.getAttributes();
        for (int i = 0; i < attribs.getLength(); i++) {
            Node attrib = attribs.item(i);
            String name = attrib.getNodeName();
            if (!name.equals("inherit")) {
                final StringBuilder beanName = new StringBuilder(name);
                for (int j = 0; j < beanName.length(); ++j) {
                    final char c = beanName.charAt(j);
                    if ('-' == c) {
                        beanName.replace(j, j + 2, String.valueOf(Character.toUpperCase(beanName.charAt(j + 1))));
                        ++j;
                    }
                }
                PropertyDescriptor descriptor = descriptors.get(beanName.toString());

                if (descriptor != null) {
                    Method setter = descriptor.getWriteMethod();

                    if (setter != null) {
                        Class<?> type = setter.getParameterTypes()[0];
                        Object value = null;
                        String valueString = attrib.getNodeValue();
                        if (type == String.class)
                            value = valueString;
                        else if (type == String[].class)
                            value = valueString.split(",");
                        else if (type == int.class || type == Integer.class)
                            value = Integer.parseInt(valueString);
                        else if (type == boolean.class || type == Boolean.class)
                            value = Boolean.parseBoolean(valueString);
                        else if (type == char.class || type == Character.class) {
                            value = valueString.charAt(0);
                            if (valueString.length() > 1)
                                LOG.error("Setting char attribute where input was more then a character long");
                        } else {
                            LOG.error("Failed to set attribute " + setter.getName() + ", unnsuported type.");
                        }
                        if (value != null) {
                            try {

                                setter.invoke(config, value);
                            } catch (Exception e) {
                                LOG.info("Failed to set attribute with name: " + setter.getName() + "(" + type + ").");
                            }
                        }
                    } else {
                        LOG.warn("Missing setter for: " + beanName.toString());
                    }
                    descriptors.remove(beanName.toString());
                } else {
                    LOG.warn("Unknown attribute in configfile: " + beanName.toString());
                }
            }
        }

        // inherited attributes
        if (inherit != null) {
            for (PropertyDescriptor d : descriptors.values()) {
                Method getter = d.getReadMethod();
                if (getter != null && getter.getDeclaringClass().isInstance(inherit)) {
                    Object value = null;

                    try {
                        value = getter.invoke(inherit);
                    } catch (Exception e) {
                        LOG.error("Failed to get value from " + inherit.getName(), e);
                    }
                    if (value != null) {
                        Method setter = d.getWriteMethod();
                        if (setter != null) {
                            try {

                                setter.invoke(config, value);

                            } catch (Exception e) {
                                LOG.error("Failed to set value from " + inherit.getName() + " on " + config.getName(),
                                        e);
                            }
                        }
                    }
                }
            }
        }
    }
}
