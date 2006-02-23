// Copyright (2006) Schibsted Søk AS
/*
 * OlympicSearchConfiguration.java
 *
 * Created on February 8, 2006, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.configuration;



/**
 *
 * @author magnuse
 */
public class OlympicSearchConfiguration extends AbstractSearchConfiguration {

    private String queryServerUrl;

    private String wikiQrServer;
    
    public OlympicSearchConfiguration() {
    }

    public String getWikiQrServer() {
        return wikiQrServer;
    }
}
