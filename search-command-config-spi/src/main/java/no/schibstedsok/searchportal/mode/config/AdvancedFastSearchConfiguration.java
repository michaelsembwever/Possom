// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * Search configuration for the advanced search command.
 *
 * @version $Id$
 */
@Controller("AdvancedFastSearchCommand")
public class AdvancedFastSearchConfiguration extends FastSearchConfiguration {

	/**
	 * Creates a new instance of the configuration.
	 *
	 */
	public AdvancedFastSearchConfiguration() {
        super(null);
	}
    
	/**
	 * Creates a new instance of the configuration.
	 * 
	 * @param asc Parent configuration.
	 */
    public AdvancedFastSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
