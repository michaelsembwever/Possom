package no.sesat.mojo.modes;

public class ConfigAbstract {
	protected String doc;
	protected String name;

	public boolean hasDoc() {
		return (doc != null && doc.trim().isEmpty() == false);
	}
}
