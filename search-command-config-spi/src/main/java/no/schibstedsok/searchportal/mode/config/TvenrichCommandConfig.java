// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * Configuration for TvEnrich search command.
 * 
 * @version $Id$
 */
@Controller("TvEnrichSearchCommand")
public final class TvenrichCommandConfig extends StaticCommandConfig {
    
    private String waitOn;
    
    /**
     * 
     * @param waitOn 
     */
    public final void setWaitOn(final String waitOn) {
        this.waitOn = waitOn;
    }
    
    /**
     * 
     * @return 
     */
    public final String getWaitOn() {
        return this.waitOn;
    }

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
                            
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "waitOn", ParseType.String, element, null);
                            
        return this;
    }
    
    
}
