// Copyright (2007) Schibsted Søk AS
/*
 * StaticSearchConfiguration.java
 *
 * Created on May 18, 2006, 10:50 AM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * Configuration for "static" search commands. That is, commands that do not
 * need to do a search but produces static HTML.
 *
 * @author maek
 * @version $Id$
 */
@Controller("StaticSearchCommand")
public class StaticSearchConfiguration extends AbstractSearchConfiguration {
    
    /**
     * 
     */
    public StaticSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public StaticSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
