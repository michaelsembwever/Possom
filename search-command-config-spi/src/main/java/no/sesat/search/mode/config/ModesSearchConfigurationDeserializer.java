package no.sesat.search.mode.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.sesat.Interpreter;
import no.sesat.Interpreter.Context;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ModesSearchConfigurationDeserializer {

    private static final Logger LOG = Logger.getLogger(ModesSearchConfigurationDeserializer.class);
    private final static Map<Class<ModesSearchConfiguration>, Map<String, MethodWrapper>> ClassMethodMap = new HashMap<Class<ModesSearchConfiguration>, Map<String, MethodWrapper>>();

    private static final String[] getters = {"get", "is"};
    private static final String[] setters = {"set", "add"};

    public static void readSearchConfiguration(final ModesSearchConfiguration config, final Element element, final SearchConfiguration inherit) {
        Set<String> methods = getMethodNames(config.getClass(), setters);

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
                setAttribute(config, beanName.toString(), attrib.getNodeValue());
                methods.remove(beanName.toString());
            }
        }

        // inherited attributes
        if (inherit != null) {
            for (String method : methods) {
                Object value = getAttribute(inherit, method);
                if (value != null) {
                    setAttribute(config, method, value);
                }
            }
        }
    }

    private static boolean startsWith(String string, String[] prefixes) {
        for (String p : prefixes) {
            if (string.startsWith(p))
                return true;
        }
        return false;
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
        } else if (klass == null) {
            return null;
        }

        Method[] methods = klass.getMethods();
        Map<String, MethodWrapper> methodMap = new HashMap<String, MethodWrapper>();
        ClassMethodMap.put(klass, methodMap);

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            if (startsWith(name, setters)) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 1) {
                    MethodWrapper.Type type = MethodWrapper.getType(paramTypes[0]);
                    if (type == MethodWrapper.Type.Unsupported) {
                        LOG
                                .trace("Unsupported type for: " + name + " "
                                        + method.getParameterTypes()[0].getSimpleName());
                    } else {

                        if (!methodMap.containsKey(name)) {
                            methodMap.put(name, new MethodWrapper(type, method));
                        } else {
                            LOG.error("Method already in map: " + name);
                        }
                    }
                }
            } else if (startsWith(name, getters) && method.getParameterTypes().length == 0) {
                MethodWrapper.Type type = MethodWrapper.getType(method.getReturnType());
                if (type == MethodWrapper.Type.Unsupported) {
                    LOG.trace("Unsupported type for: " + name + " " + method.getReturnType().getSimpleName());
                } else {
                    if (!methodMap.containsKey(name)) {
                        methodMap.put(name, new MethodWrapper(type, method));
                    } else {
                        LOG.error("Method already in map: " + name);
                    }
                }
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
    private static Set<String> getMethodNames(Class klass, String[] prefix) {
        Set<String> result = new HashSet<String>();
        Map<String, MethodWrapper> map = getMethodMap(klass);
        while (map != null) {
            for (String key : map.keySet()) {
                for (String s : prefix) {
                    if (key.startsWith(s)) {
                        result.add(key.substring(s.length()));
                    }
                }
            }

            klass = klass.getSuperclass();
            map = getMethodMap(klass);
        }
        return result;
    }

    private static void setAttribute(ModesSearchConfiguration config, final String name, final Object value) {
        MethodWrapper method = getMethodWrapper(config.getClass(), name, setters);

        if (method != null) {
            if (!method.setValue(config, value)) {
                LOG.error("Setting value on " + config.getClass().getSimpleName() + " " + name + "==" + value + " failed.");
            }
        } else {
            LOG.error("Setting value on " + config.getClass().getSimpleName() + " " + name + "==" + value
                    + " failed because method was not found.");
        }

    }

    private static Object getAttribute(final Object object, final String name) {
        MethodWrapper method = getMethodWrapper(object.getClass(), name, getters);

        return method != null ? method.getValue(object) : null;
    }

    private static MethodWrapper getMethodWrapper(Class klass, final String name, final String[] pre) {
        Map<String, MethodWrapper> map = getMethodMap(klass);
        while (map != null) {
            for (String s : pre) {
                String g = s + name.substring(0, 1).toUpperCase() + name.substring(1);
                if (map.containsKey(g)) {
                    return map.get(g);
                }
            }
            klass = klass.getSuperclass();
            map = getMethodMap(klass);
        }
        return null;
    }

    public String toString() {
        String res = getClass().getSimpleName() + " ";
        Set<String> methods = getMethodNames(getClass(), getters);
        for (String s : methods) {
            res += s + "==";
            Object o = getAttribute(this, s);
            if (o instanceof String[]) {
                res += "[";
                for (String a : (String[]) o) {
                    res += "\"" + a + "\" ";
                }
                res += "] ";
            } else if (o instanceof String) {
                res += "\"" + o + "\" ";
            } else {
                res += o + " ";
            }
        }
        return res;
    }

    /**
     * Helper class to encapsulate a method.
     */
    private static class MethodWrapper {

        enum Type {
            String, StringArray, Integer, Boolean, Char, Unsupported
        }

        private Method method;
        private final Type type;

        private MethodWrapper(Type type, Method method) {
            this.type = type;
            this.method = method;
        }

        private boolean setValue(Object obj, Object value) {
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
                    LOG.error("Failed to set attribute " + method.getName() + ", unnsuported type.");
                    return false;
                }
            }
            try {
                method.invoke(obj, value);
            } catch (Exception e) {
                LOG.info("Failed to set attribute with name: " + method.getName() + "(" + type + ").");
                return false;
            }
            return true;
        }

        private Object getValue(Object obj) {
            try {
                return method.invoke(obj);
            } catch (Exception e) {
                LOG.error("Failed to get attribute with name: " + type + " " + method.getName() + "(). "
                        + e.getMessage(), e);
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

        public String toString() {
            return method.getName() + " Type: " + type;
        }
    }

    /**
     * Add some debug function to the interpreter.
     */
    static {
        Interpreter.addFunction("scmap", new Interpreter.Function() {
            public String execute(Context ctx) {
                String res = "";
                for (Class c : ClassMethodMap.keySet()) {
                    res += "CLASS: " + c.getSimpleName() + "\n";
                    for (String s : ClassMethodMap.get(c).keySet()) {
                        res += "   " + ClassMethodMap.get(c).get(s).toString() + "\n";
                    }
                    res += "\n";
                }
                return res;
            }

            public String describe() {
                return "Print out the ClassMethodMap cache in AbstractSearchConfiguration.";
            }

        });
    }
}
