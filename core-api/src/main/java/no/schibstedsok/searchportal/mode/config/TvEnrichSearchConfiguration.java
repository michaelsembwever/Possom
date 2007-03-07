// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

/**
 * Configuration for TvEnrich search command.
 */
public class TvEnrichSearchConfiguration extends StaticSearchConfiguration {
    
    private String waitOn;
    
    public TvEnrichSearchConfiguration(){
        super(null);
    }

    public TvEnrichSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
    
    public final void setWaitOn(final String waitOn) {
        this.waitOn = waitOn;
    }
    
    public final String getWaitOn() {
        return this.waitOn;
    }
}
