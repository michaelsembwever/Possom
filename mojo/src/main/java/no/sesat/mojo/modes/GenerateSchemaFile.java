package no.sesat.mojo.modes;

/**
 * Common class for Schema Generators.
 *
 */
public abstract class GenerateSchemaFile extends GenerateFile implements Runnable {
    protected final ConfigElement root;
    protected final String id;

    private final String fileName;

    /**
     * @param element
     *            Root element
     * @param name
     *            File name
     * @param idString
     *            Id for this schema
     */
    public GenerateSchemaFile(final ConfigElement element, final String name, final String idString) {
        fileName = name;
        id = idString;
        root = element;
    }

    /**
     *
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        init(fileName);
        runImpl();
        done();
    }

    /**
     * This will be called when this runnable is run, and should generate the
     * expected schema file.
     */
    protected abstract void runImpl();
}
