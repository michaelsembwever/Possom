// Copyright (2007) Schibsted Søk AS
/*
 * DailyWordConfiguration.java
 *
 * Created on June 26, 2006, 12:00 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author maek
 * @version $Id$
 */
@Controller("DailyWordCommand")
public final class DailyWordConfiguration extends AbstractSearchConfiguration {

    /**
     * 
     * @param asc 
     */
    public DailyWordConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
