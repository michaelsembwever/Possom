/*
 * Copyright (2005-2006) Schibsted Søk AS
 */

package no.schibstedsok.searchportal.mode.config;

/**
 * @author <a href="mailto:anders@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Revision$</tt>
 */
public class AddressSearchConfiguration extends FastSearchConfiguration{
    
    public AddressSearchConfiguration() {
        super(null);
    }
    
    public AddressSearchConfiguration(final SearchConfiguration sc) {
        super(sc);
    }
}
