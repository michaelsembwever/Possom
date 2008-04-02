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
        println("<?xml version='1.0'?>");
        println("<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' id='" + id + "'>");
        indent();
        println("<xsd:element name='" + root.name + "' type='" + root.name + "'/>");
        generate(root);
        unindent();
        print("</xsd:schema>");
    }

    private void generate(final ConfigElement element) {
        if (written.add(element.name)) {
            printlnI("<xsd:complexType name='" + element.name + "'>");
            if (element.hasDoc()) {
                printlnI("<xsd:annotation>");
                printlnI("<xsd:documentation>");
                println("<![CDATA[" + element.doc + "]]>)");
                printlnU("</xsd:documentation>");
                printlnU("</xsd:annotation>");
            }

            printlnI("<xsd:choice  minOccurs='0' maxOccurs='unbounded'>");
            for (int i = 0; i < element.children.size(); i++) {
                final ConfigElement child = element.children.get(i);
                println("<xsd:element name='" + child.name + "' type='" + child.name + "'/>");
            }
            printlnU("</xsd:choice>");

            for (final Iterator<ConfigAttribute> iterator = element.attributes.iterator(); iterator.hasNext();) {
                final ConfigAttribute attrib = iterator.next();
                if (attrib.hasDoc()) {
                    printlnI("<xsd:attribute name='" + attrib.name + "'>");
                    printlnI("<xsd:annotation>");
                    printlnI("<xsd:documentation>");
                    println("<![CDATA[" + attrib.doc + "]]>)");
                    printlnU("</xsd:documentation>");
                    printlnU("</xsd:annotation>");
                    printlnU("</xsd:attribute>");
                } else {
                    println("<xsd:attribute name='" + attrib.name + "'/>");
                }
            }
            printlnU("</xsd:complexType>");

        }
        for (ConfigElement child : element.children) {
            generate(child);
        }
    }
}
