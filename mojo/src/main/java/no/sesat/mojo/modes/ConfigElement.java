/*
 * Copyright (2012) Schibsted ASA
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
package no.sesat.mojo.modes;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import java.util.ArrayList;

/**
 * Represent a class/xml element.
 *
 * @version $Id$
 */
public class ConfigElement extends AbstractConfig {

    private final List<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>();
    private final Set<String> attribNames = new TreeSet<String>();
    private final int id;
    private static int idCounter = 0;

    private List<ConfigElement> children = new ArrayList<ConfigElement>();

    public ConfigElement(final String name) {
        super(name);
        id = ++idCounter;
    }

    public ConfigElement(final String name, final String doc) {
        super(name, doc);
        id = ++idCounter;
    }

    /**
     * @param klass Class that this element should be based on.
     */
    public ConfigElement(final ClassDoc klass) {
        this(klass.name(), klass.commentText());

        // some fake attributes
        attributes.add(new ConfigAttribute("inherit"));

        build(klass);
    }

    public String getIdentifyingName(){
        return getName() + id;
    }

    /**
     * The live (original) and mutable list of attributes.
     * @return
     */
    public List<ConfigAttribute> getAttributes(){
        return attributes;
    }

    /**
     * The live (original) and mutable list of children.
     * @return
     */
    public List<ConfigElement> getChildren(){
        return children;
    }

    /**
     * @param filter filter used to modify the name
     */
    public void applyNameFilter(final NameFilter filter) {
        setName(filter.filter(getName()));
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
