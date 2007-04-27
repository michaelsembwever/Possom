/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;

/**
 * An implementation of Search Configuration for catalogue sponsed links.
 *
 * Values in configuration are injected by SearchModeFactory with value
 * from modes.xml, by the fillBeanProperty pattern. 
 *
 * @author <a href="daniele@conduct.no">Daniel Engfeldt</a>
 * @version $Id$
 */
@Controller("CatalogueAdsSearchCommand")
public final class CatalogueAdsCommandConfig extends FastCommandConfig {

    /** The name of the parameter which holds the geographic user supplied location.*/    
    private String queryParameterWhere;
    
    /**
     * 
     */
    public CatalogueAdsCommandConfig(){
    }

    /**
     *  getter for queryParameterWhere property
     * @return 
     */    
    public String getQueryParameterWhere() {
            return queryParameterWhere;
    }

    /**
     *  setter for queryParameterWhere property
     * @param queryParameterWhere 
     */    
    public void setQueryParameterWhere(String queryParameterWhere) {
            this.queryParameterWhere = queryParameterWhere;
    }    
}
