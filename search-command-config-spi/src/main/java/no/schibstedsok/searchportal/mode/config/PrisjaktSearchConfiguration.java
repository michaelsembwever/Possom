// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * Configuration for the prisjakt command.
 * 
 * @version $Id$
 */
@Controller("PrisjaktSearchCommand")
public class PrisjaktSearchConfiguration extends AbstractSearchConfiguration {
    /**
     * Creates a new instance of this search configuration.
     * @param sc 
     */
    public PrisjaktSearchConfiguration(final SearchConfiguration sc) {
        super(sc);
    }
}
