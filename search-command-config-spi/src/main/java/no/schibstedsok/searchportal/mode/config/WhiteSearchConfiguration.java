// Copyright (2006-2007) Schibsted Søk AS
/*
 * WhiteSearchConfiguration.java
 *
 * Created on March 6, 2006, 4:14 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author magnuse
 * @version $Id$
 */
@Controller("WhiteSearchCommand")
public class WhiteSearchConfiguration extends FastSearchConfiguration {

    /**
     * 
     */
    public WhiteSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public WhiteSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
