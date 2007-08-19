package no.sesat.search.site.config;

/**
 * <tt>ResourceLoader</tt> for loading java byte code from SESAT skins.
 *
 * @author magnuse
 */
public interface BytecodeLoader extends ResourceLoader {
    /**
     * Prepares and loads byte code for a class.
     *
     * @param className the class to load byte code for.
     * @param jarFileName the jar file to look in.
     */
    void initBytecodeLoader(String className, String jarFileName);

    /**
     * Returns byte code for the class.
     *
     * @return bytecode.
     */
    byte[] getBytecode();
}
