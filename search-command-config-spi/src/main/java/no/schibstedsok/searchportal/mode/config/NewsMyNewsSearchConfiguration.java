// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * 
 * @author mick
 * @version $Id$
 */
@Controller("NewsMyNewsSearchCommand")
public class NewsMyNewsSearchConfiguration extends AbstractSearchConfiguration {

    /**
     * 
     * @param sc 
     */
    public NewsMyNewsSearchConfiguration(SearchConfiguration sc) {
        super(sc);
        if (sc instanceof NewsMyNewsSearchConfiguration) {
            NewsMyNewsSearchConfiguration nmsc = (NewsMyNewsSearchConfiguration) sc;
        }
    }
}
