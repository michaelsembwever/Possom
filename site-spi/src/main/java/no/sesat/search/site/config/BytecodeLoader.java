/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
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
