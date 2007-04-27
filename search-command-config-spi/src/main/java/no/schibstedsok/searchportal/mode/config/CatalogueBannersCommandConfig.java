/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;

/**
 *
 * An implementation of Search Configuration for catalogue banner search.
 *
 * Injected by SearchModeFactory with value from modes.xml,
 * by the fillBeanProperty pattern. 
 *
 * @author Stian Hegglund
 * @version $Id$
 */
@Controller("CatalogueBannersSearchCommand")
public final class CatalogueBannersCommandConfig extends FastCommandConfig {

    /** The name of the parameter which holds the geographic user supplied location.*/
    private String queryParameterWhere;
    
    
    /**
     * 
     */
    public CatalogueBannersCommandConfig(){
    }

    /**
     *  getter for queryParameterWhere
     * @return 
     */
    public String getQueryParameterWhere() {
            return queryParameterWhere;
    }
    
    /**
     * Injected by SearchModeFactory with value from modes.xml,
     * by the fillBeanProperty pattern.
     * @param queryParameterWhere 
     */
    public void setQueryParameterWhere(String queryParameterWhere) {
            this.queryParameterWhere = queryParameterWhere;
    }    
    
}
