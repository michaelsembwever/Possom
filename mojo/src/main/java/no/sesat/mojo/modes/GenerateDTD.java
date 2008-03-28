package no.sesat.mojo.modes;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GenerateDTD extends GenerateFile implements Runnable {

	private final ConfigElement root;
	private final String fileName;
	private Set<String> written = new TreeSet<String>();

	public GenerateDTD(ConfigElement element, String name) {
		fileName = name;
		root = element;
	}

	public void run() {
		init(fileName);
		println("<?xml version='1.0' encoding='UTF-8'?>\n");
		generate(root);
		done();
	}

	private void generate(ConfigElement element) {
		if (written.add(element.name)) {

			if (element.hasDoc())
				println("<!-- " + element.doc + " -->");

			print("<!ELEMENT " + element.name);
			if (element.children.isEmpty())
				print(" EMPTY");
			else {
				print(" (");
				for (int i = 0; i < element.children.size(); i++) {
					if (i > 0)
						print("|");
					print(element.children.get(i).name);
					;
				}
				print(")*");
			}
			println(">");

			generate(element.attributes);
			printlnI("<!ATTLIST " + element.name + " ");
			for (Iterator<ConfigAttribute> iterator = element.attributes.iterator(); iterator.hasNext();) {
				ConfigAttribute attrib = (ConfigAttribute) iterator.next();
				print(attrib.name + " ");
				generate(attrib);
			}
			printlnU(">");

			for (int i = 0; i < element.children.size(); i++) {
				generate(element.children.get(i));
			}
		}
	}

	private void generate(ConfigAttribute attrib) {
		println(attrib.type + " " + (attrib.required ? "#REQUIRED" : "#IMPLIED"));
	}

	private void generate(List<ConfigAttribute> attributes) {
		println("<!--");
		for (Iterator<ConfigAttribute> iterator = attributes.iterator(); iterator.hasNext();) {
			ConfigAttribute attrib = (ConfigAttribute) iterator.next();
			print("   @attr " + attrib.name);
			if (attrib.hasDoc())
				print(" " + attrib.doc);
			println("");
		}
		println("-->");
	}
}
