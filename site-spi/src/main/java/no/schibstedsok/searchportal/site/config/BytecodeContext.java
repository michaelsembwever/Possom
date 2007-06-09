package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.searchportal.site.SiteContext;

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
