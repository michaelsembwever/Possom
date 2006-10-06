package no.schibstedsok.searchportal.mode.config;

/**
 * Search configuration for the advanced search command.
 *
 */
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
	 */public AdvancedFastSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
