package no.sesat.mojo.modes;

/**
 *
 *
 */
public class ConfigAbstract {
    protected String doc;
    protected String name;

    /**
     * @return true if it has documentation.
     */
    public boolean hasDoc() {
        return (doc != null && !doc.trim().isEmpty());
    }
}
