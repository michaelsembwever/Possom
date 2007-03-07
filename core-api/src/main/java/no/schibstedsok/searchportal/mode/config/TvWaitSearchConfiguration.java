// Copyright (2007) Schibsted Søk AS
/*
 * TvWaitSearchConfiguration.java
 *
 * Created on 26 October 2006, 14:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

/**
 *
 * @author andersjj
 */
public class TvWaitSearchConfiguration extends FastSearchConfiguration {
    /** Modifier index to use */
    private int index = 0;
    
    /** Name of search command to wait on */
    private String waitOn;

    /** Turn on/off cookie channel selection */
    private boolean useMyChannels = false;
    
    /**
     * Creates a new instance of TvWaitSearchConfiguration
     */
    public TvWaitSearchConfiguration(final SearchConfiguration asc) {
        super(asc);
    }
    
    public final void setIndex(final int index) {
        this.index = index;
    }
    
    public final int getIndex() {
        return index;
    }
    
    public final void setWaitOn(final String waitOn) {
        this.waitOn = waitOn;
    }
    
    public final String getWaitOn() {
        return waitOn;
    }
    
    public final void setUseMyChannels(final boolean useMyChannels) {
        this.useMyChannels = useMyChannels;
    }
    
    public final boolean getUseMyChannels() {
        return useMyChannels;
    }
}
