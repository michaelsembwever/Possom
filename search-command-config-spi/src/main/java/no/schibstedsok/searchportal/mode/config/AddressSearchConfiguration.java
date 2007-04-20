/*
 * Copyright (2005-2007) Schibsted Søk AS
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * @author <a href="mailto:anders@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Id$</tt>
 */
@Controller("AddressSearchCommand")
public class AddressSearchConfiguration extends FastSearchConfiguration{
    
    /**
     * 
     */
    public AddressSearchConfiguration() {
        super(null);
    }
    
    /**
     * 
     * @param sc 
     */
    public AddressSearchConfiguration(final SearchConfiguration sc) {
        super(sc);
    }
}
