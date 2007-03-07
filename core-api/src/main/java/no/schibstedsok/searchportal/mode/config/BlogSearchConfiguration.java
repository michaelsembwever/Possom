// Copyright (2007) Schibsted Søk AS

/*
 * BlogSearchConfiguration.java
 *
 * Created on July 12, 2006, 10:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

/**
 *
 * @author maek
 */
public class BlogSearchConfiguration extends NavigatableESPFastConfiguration {
    
    public BlogSearchConfiguration(){
        super(null);
    }

    public BlogSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
