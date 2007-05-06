package no.schibstedsok.searchportal.site.config;

/**
 * 
 */
public interface BytecodeLoader extends ResourceLoader {
    void initBytecodeLoader(String className);
    byte[] getBytecode();
}
