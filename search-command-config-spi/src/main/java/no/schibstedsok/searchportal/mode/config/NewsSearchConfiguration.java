// Copyright (2006-2007) Schibsted Søk AS
/*
 * NewsSearchConfiguration.java
 *
 * Created on March 7, 2006, 5:42 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author magnuse
 * @version $Id$
 */
@Controller("NewsSearchCommand")
public class NewsSearchConfiguration extends FastSearchConfiguration {

    /**
     * 
     */
    public NewsSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public NewsSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
