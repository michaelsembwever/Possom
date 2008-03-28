package no.sesat.mojo.modes;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class GenerateXSD extends GenerateFile implements Runnable {

	private final ConfigElement root;
	private final String fileName;
	private Set<String> written = new TreeSet<String>();

	public GenerateXSD(ConfigElement element, String name) {
		fileName = name;
		root = element;
	}

	public void run() {
		init(fileName);

		println("<?xml version='1.0'?>");
		println("<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>");
		indent();
		println("<xsd:element name='" + root.name + "' type='" + root.name + "'/>");
		generate(root);
		unindent();
		print("</xsd:schema>");

		done();
	}

	private void generate(ConfigElement element) {
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
				ConfigElement child = element.children.get(i);
				println("<xsd:element name='" + child.name + "' type='" + child.name + "'/>");
			}
			printlnU("</xsd:choice>");

			for (Iterator<ConfigAttribute> iterator = element.attributes.iterator(); iterator.hasNext();) {
				ConfigAttribute attrib = (ConfigAttribute) iterator.next();
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
