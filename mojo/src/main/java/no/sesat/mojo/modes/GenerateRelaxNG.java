package no.sesat.mojo.modes;

import java.util.Iterator;

public class GenerateRelaxNG extends GenerateFile implements Runnable {

	private final ConfigElement root;
	private final String fileName;

	public GenerateRelaxNG(ConfigElement element, String name) {
		fileName = name;
		root = element;
	}

	public void run() {
		init(fileName);
		generate(root);
		done();
	}

	private void generate(ConfigElement element) {

		if (element.hasDoc()) {
			String[] docArray = element.doc.split("\n");
			for (int i = 0; i < docArray.length; i++) {
				println("## " + docArray[i]);
			}
		}

		println("element " + element.name + " {");
		indent();
		if (element.attributes.isEmpty() && element.children.isEmpty()) {
			print(" empty ");
		} else {
			for (Iterator<ConfigAttribute> iterator = element.attributes.iterator(); iterator.hasNext();) {
				ConfigAttribute attrib = (ConfigAttribute) iterator.next();

				generate(attrib);
				if (iterator.hasNext() || !element.children.isEmpty()) {
					println(",");
				} else {
					println("");
				}
			}
		}

		if (!element.children.isEmpty()) {
			println("(");
			for (int i = 0; i < element.children.size(); i++) {
				if (i > 0) {
					println("|");
				}
				generate(element.children.get(i));
			}
			println(")*");
		}
		unindent();
		println("}*");

	}

	private void generate(ConfigAttribute attrib) {
		if (attrib.hasDoc()) {
			String[] docArray = attrib.doc.split("\n");
			for (int i = 0; i < docArray.length; i++) {
				println("## " + docArray[i]);
			}
		}
		print("attribute " + attrib.name + " { text }" + (attrib.required ? "" : "?"));
	}

}
