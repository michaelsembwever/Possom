package no.sesat.mojo.modes;

import com.sun.javadoc.MethodDoc;

public class ConfigAttribute extends ConfigAbstract {
	protected String type = "CDATA";
	protected boolean required = false;

	public ConfigAttribute(MethodDoc method) {
		doc = parseDoc(method);

		name = Builder.toXmlName(method.name()).substring(4);
		type = "CDATA"; // method.parameters()[0].toString();
	}

	protected ConfigAttribute(String name) {
		this.name = name;
	}

	protected ConfigAttribute(String name, String doc) {
		this.name = name;
		this.doc = doc;
	}

	protected ConfigAttribute(String name, String doc, boolean required) {
		this.name = name;
		this.doc = doc;
		this.required = required;
	}

	private String parseDoc(MethodDoc method) {
		if (method == null)
			return null;
		if (method.commentText().contains("{@inheritDoc}"))
			return parseDoc(method.overriddenMethod());
		else
			return method.commentText();

	}

}
