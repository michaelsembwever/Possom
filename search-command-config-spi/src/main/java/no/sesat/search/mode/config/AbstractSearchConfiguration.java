/* Copyright (2008-2008) Schibsted Søk AS
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

import java.lang.reflect.Method;
import java.util.*;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class AbstractSearchConfiguration {
    private static final Logger LOG = Logger.getLogger(AbstractSearchConfiguration.class);
    private final static Map<Class, Map<String, MethodWrapper>> ClassMethodMap = new HashMap<Class, Map<String, MethodWrapper>>();

    /**
     * This method will be called before settings from the modes.xml files will be applied.
     * Default implementation is empty.
     *
     * @param element The xml element where the attribues are found.
     * @param inherit The configuration that we inherit from.
     */
    protected void readSearchConfigurationBefore(final Element element, final SearchConfiguration inherit) {

    }

    /**
     * This method will be called after settings from the modes.xml files will be applied.
     * Default implementation is empty.
     *
     * @param element The xml element where the attribues are found.
     * @param inherit The configuration that we inherit from.
     */
    protected void readSearchConfigurationAfter(final Element element, final SearchConfiguration inherit) {

    }


    /**
     * This method will apply the attributes found in element.
     *
     * @param element The xml element where the attribues are found.
     * @param inherit The configuration that we inherit from.
     */
    public final void readSearchConfiguration(final Element element, final SearchConfiguration inherit) {
        readSearchConfigurationBefore(element, inherit);
        Set<String> methods = getMethodNames();

        NamedNodeMap attribs = element.getAttributes();
        for (int i = 0; i < attribs.getLength(); i++) {
            Node attrib = attribs.item(i);
            String name = attrib.getNodeName();
            if (!name.equals("inherit")) {
                final StringBuilder beanName = new StringBuilder(name);
                for (int j = 0; j < beanName.length(); ++j) {
                    final char c = beanName.charAt(j);
                    if (j == 0) {
                        beanName.replace(j, j + 1, String.valueOf(Character.toUpperCase(c)));
                    } else if ('-' == c) {
                        beanName.replace(j, j + 2, String.valueOf(Character.toUpperCase(beanName.charAt(j + 1))));
                        ++j;
                    }
                }
                setAttribute(beanName.toString(), attrib.getNodeValue());
                methods.remove(beanName.toString());
            }
        }

        // inherited attributes
        if (inherit != null) {
            for (String method : methods) {
                Object value = getAttribute(inherit, method);
                if (value != null) {
                    setAttribute(method, value);
                }
            }
        }
        readSearchConfigurationAfter(element, inherit);
    }

    /**
     * Fetch the method map for klass, if it has not been initialized this will
     * be done first.
     *
     * @param klass
     * @return A mapping between name and the MethodWrapper.
     */
    private static Map<String, MethodWrapper> getMethodMap(Class klass) {
        if (ClassMethodMap.containsKey(klass)) {
            return ClassMethodMap.get(klass);
        } else if (klass == null || klass == AbstractSearchConfiguration.class) {
            return null;
        }

        Method[] methods = klass.getMethods();
        Map<String, MethodWrapper> methodMap = new HashMap<String, MethodWrapper>();
        ClassMethodMap.put(klass, methodMap);

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            String shortName = name.substring(3);
            if (name.startsWith("set") || name.startsWith("add")) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 1) {
                    MethodWrapper.Type type = MethodWrapper.getType(paramTypes[0]);


                    if (type == MethodWrapper.Type.Unsupported) {
                        LOG.info("Unsupported type for: " + name + " " + method.getParameterTypes()[0].getSimpleName());
                    } else {

                        if (!methodMap.containsKey(shortName)) {
                            methodMap.put(shortName, new MethodWrapper(type));
                        }

                        methodMap.get(shortName).setMethod = method;
                    }
                }
            } else if (name.startsWith("get") && method.getParameterTypes().length == 0) {

                MethodWrapper.Type type = MethodWrapper.getType(method.getReturnType());

                if (!methodMap.containsKey(shortName)) {
                    methodMap.put(shortName, new MethodWrapper(type));
                }

                methodMap.get(shortName).getMethod = method;

            }
        }
        getMethodMap(klass.getSuperclass());
        return methodMap;
    }

    /**
     * All method wrappers found for this class.
     *
     * @return Set with all method wrappers found for this class.
     */
    private Set<String> getMethodNames() {
        Set<String> result = new HashSet<String>();
        Class klass = getClass();
        Map map = getMethodMap(klass);
        while (map != null) {
            result.addAll(map.keySet());
            klass = klass.getSuperclass();
            map = getMethodMap(klass);
        }
        return result;
    }

    private void setAttribute(final String name, final Object value) {
        Class klass = getClass();
        MethodWrapper method = getMethodWrapper(klass, name);

        if (method != null) {
            if (method.setValue(this, value)) {
                LOG.info("Setting value on " + getClass() + " " + name + "==" + value);
            } else {
                LOG.info("Setting value on " + getClass() + " " + name + "==" + value
                        + " failed.");
            }
        }
        return;
    }

    private static Object getAttribute(final Object object, final String name) {
        MethodWrapper method = getMethodWrapper(object.getClass(), name);

        return method != null ? method.getValue(object) : null;
    }

    private static MethodWrapper getMethodWrapper(Class klass, final String name) {
        Map<String, MethodWrapper> map = getMethodMap(klass);
        while (map != null) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
            klass = klass.getSuperclass();
            map = getMethodMap(klass);
        }
        LOG.info("Could not find method with name: " + name + " in class " + klass.getSimpleName());
        return null;
    }

    /**
     * Helper class to encapsulate a method.
     */
    private static class MethodWrapper {

        enum Type {
            String, StringArray, Integer, Boolean, Char, Unsupported
        }

        private Method setMethod;
        private Method getMethod;
        private final Type type;

        private MethodWrapper(Type type) {
            this.type = type;
        }

        private boolean setValue(Object obj, Object value) {
            if (setMethod != null) {
                if (value instanceof String) {
                    String valueString = (String) value;
                    switch (type) {
                        case String:
                            break;
                        case StringArray:
                            value = valueString.split(",");
                            break;
                        case Integer:
                            value = Integer.parseInt(valueString);
                            break;
                        case Boolean:
                            value = Boolean.parseBoolean(valueString);
                            break;
                        case Char:
                            value = valueString.charAt(0);
                            if (valueString.length() > 1)
                                LOG.error("Setting char attribute where input was more then a character long");
                            break;
                        default:
                            LOG.error("Failed to set attribute " + setMethod.getName() + ", unnsuported type.");
                            return false;
                    }
                }
                try {
                    setMethod.invoke(obj, value);
                } catch (Exception e) {
                    LOG.info("Failed to set attribute with name: " + setMethod.getName() + "(" + type + ").");
                    return false;
                }
            } else {
                LOG.info("Failed to set attribute, set method == null.");

                return false;
            }
            return true;
        }

        private Object getValue(Object obj) {

            if (getMethod != null) {

                try {
                    Object res = getMethod.invoke(obj);
                    LOG.info("getting " + getMethod.getName() + " --> " + res);
                    return res;
                } catch (Exception e) {
                    LOG.error("Failed to get attribute with name: " + type + " " + setMethod.getName() + "(). " + e.getMessage(), e);
                }

            }
            return null;
        }

        private static Type getType(Class klass) {
            Type type;
            String simpleName = klass.getSimpleName();

            if (simpleName.equals("String"))
                type = MethodWrapper.Type.String;
            else if (simpleName.equals("String[]"))
                type = MethodWrapper.Type.StringArray;
            else if (simpleName.equals("int") || simpleName.equals("Integer"))
                type = MethodWrapper.Type.Integer;
            else if (simpleName.equals("boolean") || simpleName.equals("Boolean"))
                type = MethodWrapper.Type.Boolean;
            else if (simpleName.equals("char") || simpleName.equals("Character"))
                type = MethodWrapper.Type.Char;
            else
                type = MethodWrapper.Type.Unsupported;
            return type;
        }
    }
}

