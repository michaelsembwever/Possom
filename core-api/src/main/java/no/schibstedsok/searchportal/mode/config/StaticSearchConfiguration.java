// Copyright (2007) Schibsted Søk AS
/*
 * StaticSearchConfiguration.java
 *
 * Created on May 18, 2006, 10:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

/**
 * Configuration for "static" search commands. That is, commands that do not
 * need to do a search but produces static HTML.
 *
 * @author maek
 */
public class StaticSearchConfiguration extends AbstractSearchConfiguration {
    
    public StaticSearchConfiguration(){
        super(null);
    }

    public StaticSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
}
