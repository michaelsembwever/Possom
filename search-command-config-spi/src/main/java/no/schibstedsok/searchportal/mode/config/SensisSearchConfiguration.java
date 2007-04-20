// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("SensisSearchCommand")
public class SensisSearchConfiguration extends FastSearchConfiguration {

    /**
     * 
     */
    public SensisSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public SensisSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
