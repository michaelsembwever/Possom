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
import java.util.Set;
import java.util.TreeSet;

/**
 * Generator for XML Schema.
 */
public class GenerateXSD extends GenerateSchemaFile {
    private final Set<String> written = new TreeSet<String>();

    /**
     * @param element
     *            Root element.
     * @param name
     *            File name.
     * @param idString
     *            id.
     */
    protected GenerateXSD(final ConfigElement element, final String name, final String idString) {
        super(element, name, idString);
    }

    /**
     * Generate XML Schema.
     */
    @Override
    protected void runImpl() {
        writeln("<?xml version='1.0'?>");
        writeln("<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' id='" + id + "'>");
        indent();
        writeln("<xsd:element name='" + root.getName() + "' type='" + root.getIdentifyingName() + "'/>");
        generate(root);
        unindent();
        write("</xsd:schema>");
    }

    private void generate(final ConfigElement element) {
        if (written.add(element.getIdentifyingName())) {
            writelnI("<xsd:complexType name='" + element.getIdentifyingName() + "'>");
            if (element.hasDoc()) {
                writelnI("<xsd:annotation>");
                writelnI("<xsd:documentation>");
                writeln("<![CDATA[" + element.getDoc() + "]]>)");
                writelnU("</xsd:documentation>");
                writelnU("</xsd:annotation>");
            }

            writelnI("<xsd:choice  minOccurs='0' maxOccurs='unbounded'>");
            for (int i = 0; i < element.getChildren().size(); i++) {
                final ConfigElement child = element.getChildren().get(i);
                writeln("<xsd:element name='" + child.getName() + "' type='" + child.getIdentifyingName() + "'/>");
            }
            writelnU("</xsd:choice>");

            for (final Iterator<ConfigAttribute> iterator = element.getAttributes().iterator(); iterator.hasNext();) {
                final ConfigAttribute attrib = iterator.next();
                if (attrib.hasDoc()) {
                    writelnI("<xsd:attribute name='" + attrib.getName() + "'>");
                    writelnI("<xsd:annotation>");
                    writelnI("<xsd:documentation>");
                    writeln("<![CDATA[" + attrib.getDoc() + "]]>)");
                    writelnU("</xsd:documentation>");
                    writelnU("</xsd:annotation>");
                    writelnU("</xsd:attribute>");
                } else {
                    writeln("<xsd:attribute name='" + attrib.getName() + "'/>");
                }
            }
            writelnU("</xsd:complexType>");

        }
        for (ConfigElement child : element.getChildren()) {
            if (!written.contains(child.getIdentifyingName())) {
                generate(child);
            }
        }
    }
}
