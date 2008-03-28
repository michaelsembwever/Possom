package no.sesat.mojo.modes;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;

public class ConfigElement extends ConfigAbstract {

	final protected List<ConfigAttribute> attributes = new Vector<ConfigAttribute>();
	final private Set<String> attribNames = new TreeSet<String>();
	final protected int id;
	private static int idCounter = 0;

	protected List<ConfigElement> children = new Vector<ConfigElement>();

	public ConfigElement(String name) {
		id = ++idCounter;
		this.name = name;
	}

	public ConfigElement(ClassDoc klass) {
		this(klass.name());

		doc = klass.commentText();

		// some fake attributes
		attributes.add(new ConfigAttribute("id", "fix doc", true));
		attributes.add(new ConfigAttribute("inherit"));
		attributes.add(new ConfigAttribute("result-fields"));
		attributes.add(new ConfigAttribute("field-filters"));
		attributes.add(new ConfigAttribute("collections"));
		build(klass);
	}

	public void applyNameFilter(NameFilter filter) {
		name = filter.filter(name);
	}

	public void addChildren(List<ConfigElement> children) {
		this.children.addAll(children);
	}

	public void addChild(ConfigElement child) {
		this.children.add(child);
	}

	private void build(ClassDoc klass) {

		if (klass != null) {
			MethodDoc[] methods = klass.methods();
			for (int i = 0; i < methods.length; i++) {

				MethodDoc methodDoc = methods[i];

				if (!attribNames.contains(methodDoc.name()) && (methodDoc.name().startsWith("set") || methodDoc.name().startsWith("add"))) {
					Parameter parameters[] = methodDoc.parameters();
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