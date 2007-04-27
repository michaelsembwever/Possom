// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;

/**
 * Configuration for TvEnrich search command.
 * 
 * @version $Id$
 */
@Controller("TvEnrichSearchCommand")
public class TvenrichCommandConfig extends StaticCommandConfig {
    
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
}
