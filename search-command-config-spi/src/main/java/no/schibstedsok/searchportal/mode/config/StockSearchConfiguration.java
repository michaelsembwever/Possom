// Copyright (2006-2007) Schibsted Søk AS
/*
 * StockSearchConfiguration.java
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author magnuse
 * @version $Id$
 */
@Controller("StockSearchCommand")
public class StockSearchConfiguration extends AbstractSearchConfiguration {

    /**
     * 
     */
    public StockSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public StockSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

}
