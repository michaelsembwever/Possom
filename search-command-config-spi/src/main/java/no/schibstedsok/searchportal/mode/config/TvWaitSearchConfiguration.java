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

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author andersjj
 * @version $Id$
 */
@Controller("TvWaitSearchCommand")
public class TvWaitSearchConfiguration extends FastSearchConfiguration {
    /** Modifier index to use */
    private int index = 0;
    
    /** Name of search command to wait on */
    private String waitOn;

    /** Turn on/off cookie channel selection */
    private boolean useMyChannels = false;
    
    /**
     * Creates a new instance of TvWaitSearchConfiguration
     * @param asc 
     */
    public TvWaitSearchConfiguration(final SearchConfiguration asc) {
        super(asc);
    }
    
    /**
     * 
     * @param index 
     */
    public final void setIndex(final int index) {
        this.index = index;
    }
    
    /**
     * 
     * @return 
     */
    public final int getIndex() {
        return index;
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
        return waitOn;
    }
    
    /**
     * 
     * @param useMyChannels 
     */
    public final void setUseMyChannels(final boolean useMyChannels) {
        this.useMyChannels = useMyChannels;
    }
    
    /**
     * 
     * @return 
     */
    public final boolean getUseMyChannels() {
        return useMyChannels;
    }
}
