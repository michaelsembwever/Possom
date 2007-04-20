// Copyright (2007) Schibsted Søk AS
/*
 * BlogSearchConfiguration.java
 *
 * Created on July 12, 2006, 10:35 AM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author maek
 * @version $Id$
 */
@Controller("BlogSearchCommand")
public final class BlogSearchConfiguration extends NavigatableESPFastConfiguration {
    
    /**
     * 
     */
    public BlogSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public BlogSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
