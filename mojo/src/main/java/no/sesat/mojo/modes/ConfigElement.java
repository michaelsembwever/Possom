package no.sesat.mojo.modes;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;

/**
 * Represent a class/xml element.
 *
 */
public class ConfigElement extends ConfigAbstract {

    protected final List<ConfigAttribute> attributes = new Vector<ConfigAttribute>();
    private final Set<String> attribNames = new TreeSet<String>();
    protected final int id;
    private static int idCounter = 0;

    protected List<ConfigElement> children = new Vector<ConfigElement>();

    /**
     * @param name Name of this element.
     */
    public ConfigElement(final String name) {
        id = ++idCounter;
        this.name = name;
    }

    /**
     * @param klass Class that this element should be based on.
     */
    public ConfigElement(final ClassDoc klass) {
        this(klass.name());

        doc = klass.commentText();

        // some fake attributes
        attributes.add(new ConfigAttribute("inherit"));

        build(klass);
    }

    /**
     * @param filter filter used to modify the name
     */
    public void applyNameFilter(final NameFilter filter) {
        name = filter.filter(name);
    }

    /**
     * @param childrenList children that we want to add.
     */
    public void addChildren(final List<ConfigElement> childrenList) {
        children.addAll(childrenList);
    }

    /**
     * @param child child that we want to add.
     */
    public void addChild(final ConfigElement child) {
        children.add(child);
    }

    private void build(final ClassDoc klass) {

        if (klass != null) {
            final MethodDoc[] methods = klass.methods();
            for (int i = 0; i < methods.length; i++) {

                final MethodDoc methodDoc = methods[i];

                if (!attribNames.contains(methodDoc.name())
                        && (methodDoc.name().startsWith("set") || methodDoc.name().startsWith("add"))) {
                    final Parameter[] parameters = methodDoc.parameters();
                    if (parameters.length == 1) {
                        attribNames.add(methodDoc.name());
                        attributes.add(new ConfigAttribute(methodDoc));

                    }
                }
            }
            build(klass.superclass());
        }
    }
}
