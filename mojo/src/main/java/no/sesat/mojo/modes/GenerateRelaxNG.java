/*
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
package no.sesat.mojo.modes;

import java.util.Iterator;

/**
 * Generator for Relax NG.
 */
public class GenerateRelaxNG extends GenerateSchemaFile {

    /**
     * @param element
     *            Root element.
     * @param name
     *            File name.
     * @param idString
     *            id.
     */
    protected GenerateRelaxNG(final ConfigElement element, final String name, final String idString) {
        super(element, name, idString);
    }

    /**
     * Generate Relax NG.
     */
    @Override
    protected void runImpl() {
        generate(root);
    }

    private void generate(final ConfigElement element) {

        if (element.hasDoc()) {
            final String[] docArray = element.getDoc().split("\n");
            for (int i = 0; i < docArray.length; i++) {
                writeln("## " + docArray[i]);
            }
        }

        // prevent blowing the stack. This is because we currently don't support
        // recursive elements in this RelaxNG generator.
        boolean empty = true;
        for (ConfigElement child : element.getChildren()) {
            if (!element.getName().equals(child.getName())) {
                empty = false;
            }
        }

        writeln("element " + element.getName() + " {");
        indent();
        if (element.getAttributes().isEmpty() && empty) {
            write(" empty ");
        } else {
            for (final Iterator<ConfigAttribute> iterator = element.getAttributes().iterator(); iterator.hasNext();) {
                final ConfigAttribute attrib = iterator.next();

                generate(attrib);
                if (iterator.hasNext() || !empty) {
                    writeln(",");
                } else {
                    writeln("");
                }
            }
        }

        if (!empty) {
            writeln("(");
            boolean one = false;
            for (ConfigElement child : element.getChildren()) {
                if (!element.getName().equals(child.getName())) {
                    if(one) {
                        writeln("|");
                    } else {
                        one = true;
                    }
                    generate(child);
                }
            }
            writeln(")*");
        }
        unindent();
        writeln("}*");

    }

    private void generate(final ConfigAttribute attrib) {
        if (attrib.hasDoc()) {
            final String[] docArray = attrib.getDoc().split("\n");
            for (int i = 0; i < docArray.length; i++) {
                writeln("## " + docArray[i]);
            }
        }
        write("attribute " + attrib.getName() + " { text }" + (attrib.isRequired() ? "" : "?"));
    }

}
