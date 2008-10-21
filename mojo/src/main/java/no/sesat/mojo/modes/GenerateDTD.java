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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generator for DTD.
 *
 */
public class GenerateDTD extends GenerateSchemaFile {
    private final Set<String> written = new TreeSet<String>();

    /**
     * @param element
     *            Root element.
     * @param name
     *            File name.
     * @param idString
     *            id.
     */
    public GenerateDTD(final ConfigElement element, final String name, final String idString) {
        super(element, name, idString);
    }

    /**
     * Generate DTD.
     */
    @Override
    public void runImpl() {
        writeln("<?xml version='1.0' encoding='UTF-8'?>\n");
        writeln("<!-- " + id + " -->");
        generate(root);
    }

    private void generate(final ConfigElement element) {
        if (written.add(element.getName())) {

            if (element.hasDoc()) {
                writeln("<!-- " + element.getDoc() + " -->");
            }

            write("<!ELEMENT " + element.getName());
            if (element.getChildren().isEmpty()) {
                write(" EMPTY");
            } else {
                write(" (");
                for (int i = 0; i < element.getChildren().size(); i++) {
                    if (i > 0) {
                        write("|");
                    }
                    write(element.getChildren().get(i).getName());
                }
                write(")*");
            }
            writeln(">");

            generate(element.getAttributes());
            writelnI("<!ATTLIST " + element.getName() + " ");
            for (final Iterator<ConfigAttribute> iterator = element.getAttributes().iterator(); iterator.hasNext();) {
                final ConfigAttribute attrib = iterator.next();
                write(attrib.getName() + " ");
                generate(attrib);
            }
            writelnU(">");

            for (ConfigElement child : element.getChildren()) {
                if (!written.contains(child.getName())) {
                    generate(child);
                }
            }
        }
    }

    private void generate(final ConfigAttribute attrib) {
        writeln(attrib.getType() + " " + (attrib.isRequired() ? "#REQUIRED" : "#IMPLIED"));
    }

    private void generate(final List<ConfigAttribute> attributes) {
        writeln("<!--");
        for (final Iterator<ConfigAttribute> iterator = attributes.iterator(); iterator.hasNext();) {
            final ConfigAttribute attrib = iterator.next();
            write("   @attr " + attrib.getName());
            if (attrib.hasDoc()) {
                write(" " + attrib.getDoc());
            }
            writeln("");
        }
        writeln("-->");
    }
}
