// Copyright (2007) Schibsted Søk AS
/*
 * TvwaitsearchCommandConfig.java
 *
 * Created on 26 October 2006, 14:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 *
 * @author andersjj
 * @version $Id$
 */
@Controller("TvWaitSearchCommand")
public final class TvwaitsearchCommandConfig extends FastCommandConfig {
    
    /** Modifier index to use */
    private int index = 0;
    
    /** Name of search command to wait on */
    private String waitOn;

    /** Turn on/off cookie channel selection */
    private boolean useMyChannels = false;
        
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

    @Override
    public FastCommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "index", ParseType.Int, element, "0");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "waitOn", ParseType.String, element, null);
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "useMyChannels", ParseType.Boolean, element, "false");

        return this;
    }

    
}
