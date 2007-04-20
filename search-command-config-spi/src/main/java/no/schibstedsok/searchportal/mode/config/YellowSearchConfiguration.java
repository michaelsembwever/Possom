/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * An implementation of Search Configuration for yellow searches.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Id$
 */
@Controller("YellowSearchCommand")
public class YellowSearchConfiguration extends FastSearchConfiguration {

    /**
     * 
     */
    public YellowSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public YellowSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

}
