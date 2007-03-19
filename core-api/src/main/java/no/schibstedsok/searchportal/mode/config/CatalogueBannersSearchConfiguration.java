/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

/**
 *
 * An implementation of Search Configuration for catalogue banner search.
 *
 * Injected by SearchModeFactory with value from modes.xml,
 * by the fillBeanProperty pattern. 
 *
 * @author Stian Hegglund
 * @version $Revision:$
 */
public class CatalogueBannersSearchConfiguration extends FastSearchConfiguration {

    /** The name of the parameter which holds the geographic user supplied location.*/
    private String queryParameterWhere;
    
    
    public CatalogueBannersSearchConfiguration(){
        super(null);
    }

    public CatalogueBannersSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    
    /**
     *  getter for queryParameterWhere
     */
    public String getQueryParameterWhere() {
            return queryParameterWhere;
    }
    
    /**
     * Injected by SearchModeFactory with value from modes.xml,
     * by the fillBeanProperty pattern.
     */
    public void setQueryParameterWhere(String queryParameterWhere) {
            this.queryParameterWhere = queryParameterWhere;
    }    
    
}
