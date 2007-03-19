/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

/**
 * An implementation of Search Configuration for catalogue sponsed links.
 *
 * Values in configuration are injected by SearchModeFactory with value
 * from modes.xml, by the fillBeanProperty pattern. 
 *
 * @author <a href="daniele@conduct.no">Daniel Engfeldt</a>
 * @version $Revision: 1 $
 */
public class CatalogueAdsSearchConfiguration extends FastSearchConfiguration {

    /** The name of the parameter which holds the geographic user supplied location.*/    
    private String queryParameterWhere;
    
    public CatalogueAdsSearchConfiguration(){
        super(null);
    }

    public CatalogueAdsSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
        
    /**
     *  getter for queryParameterWhere property
     */    
    public String getQueryParameterWhere() {
            return queryParameterWhere;
    }

    /**
     *  setter for queryParameterWhere property
     */    
    public void setQueryParameterWhere(String queryParameterWhere) {
            this.queryParameterWhere = queryParameterWhere;
    }    
}
