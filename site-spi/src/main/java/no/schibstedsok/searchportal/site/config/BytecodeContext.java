package no.schibstedsok.searchportal.site.config;

import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.searchportal.site.SiteContext;

public interface BytecodeContext extends BaseContext {
    public BytecodeLoader newBytecodeLoader(SiteContext context, String className);
}
