package no.schibstedsok.searchportal.site.config;

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
     */
    void initBytecodeLoader(String className);

    /**
     * Returns byte code for the class.
     *
     * @return bytecode.
     */
    byte[] getBytecode();
}
