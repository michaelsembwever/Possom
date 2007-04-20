// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * Configuration for TvEnrich search command.
 * 
 * @version $Id$
 */
@Controller("TvEnrichSearchCommand")
public class TvEnrichSearchConfiguration extends StaticSearchConfiguration {
    
    private String waitOn;
    
    /**
     * 
     */
    public TvEnrichSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public TvEnrichSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
    
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
