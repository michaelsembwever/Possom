/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.site.config;

import no.schibstedsok.commons.ioc.BaseContext;
import no.sesat.search.site.SiteContext;

/**
 * Interface providing a way to get bytecode resource loaders.
 *
 * @author magnuse
 */
public interface BytecodeContext extends BaseContext {
    /**
     * Returns a loader for the site and class. If a jarFileName is supplied only that jar file will be used to find the
     * class.
     *
     * @param siteContext the site to load bytecode for
     * @param className the class to load.
     * @return byte code for class.
     * @param jarFileName optional jar file to restrict loader to.
     */
    public BytecodeLoader newBytecodeLoader(SiteContext siteContext, String className, String jarFileName);
}
