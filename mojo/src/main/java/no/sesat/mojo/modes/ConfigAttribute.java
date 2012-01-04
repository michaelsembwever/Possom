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

import com.sun.javadoc.MethodDoc;

/**
 * Data representing an attribute.
 *
 * @version $Id$
 */
public class ConfigAttribute extends AbstractConfig {

    private String type = "CDATA";
    private boolean required = false;

    public ConfigAttribute(final String name) {
        super(name);
    }

    /**
     * @param method Construct this attribute from a Javadoc element.
     */
    public ConfigAttribute(final MethodDoc method) {
        super(Builder.toXmlName(method.name()).substring(4) ,parseDoc(method));

        type = "CDATA"; // method.parameters()[0].toString();
    }

    /**
     * @param name
     *            Name of this attribute.
     * @param doc
     *            Doc for this attribute.
     * @param required
     *            if this is required attribute or not
     */
    protected ConfigAttribute(final String name, final String doc, final boolean required) {
        super(name, doc);
        this.required = required;
    }

    public String getType(){
        return type;
    }

    public boolean isRequired(){
        return required;
    }

    private static String parseDoc(final MethodDoc method) {

        if (method == null) {
            return null;
        }
        if (method.commentText().contains("{@inheritDoc}")) {
            return parseDoc(method.overriddenMethod());
        } else {
            return method.commentText();
        }
    }
}
