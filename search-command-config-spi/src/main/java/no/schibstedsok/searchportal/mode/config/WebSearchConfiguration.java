// Copyright (2007) Schibsted Søk AS
/*
 * WebSearchConfiguration.java
 *
 * Created on March 7, 2006, 1:28 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author magnuse
 * @version $Id$
 */
@Controller("WebSearchCommand")
public final class WebSearchConfiguration extends FastSearchConfiguration {

    /**
     * 
     */
    public WebSearchConfiguration(){
        super(null);
    }
    
    /**
     * 
     * @param asc 
     */
    public WebSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
