// Copyright (2007) Schibsted Søk AS
/*
 * YellowGeoSearchConfiguration.java
 *
 * Created on 17. august 2006, 10:54
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author ssthkjer
 *  @version $Id$
 */
@Controller("YellowGeoSearchCommand")
public class YellowGeoSearchConfiguration extends FastSearchConfiguration {
    
    /** Creates a new instance of YellowGeoSearchConfiguration */
    public YellowGeoSearchConfiguration() {
        super(null);
    }
    
    /**
     * 
     * @param asc 
     */
    public YellowGeoSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
    
}
