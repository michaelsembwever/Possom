/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

/**
 * An implementation of Search Configuration for yellow searches.
 *
 * Values in configuration are injected by SearchModeFactory with value
 * from modes.xml, by the fillBeanProperty pattern.
 *
 * @author <a href="larsj@conduct.no">Lars Johansson</a>
 * @version $Revision:$
 */
public class CatalogueSearchConfiguration extends FastSearchConfiguration {
    
    /** The name of the parameter which holds the geographic user supplied location.*/
    private String queryParameterWhere;
    
    
    /**
     *  ????
     */
    private String searchBy;
    
    /**
     * If split is set to true in modes.xml, the CatalogueSearchCommand,
     * will try to split the q-parameter into catalogueWhere and catalogueWhat,
     * based on recognised geographic locations.
     */
    private Boolean split;
    
    public CatalogueSearchConfiguration(){
        super(null);
    }
    
    public CatalogueSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }
    
    
    
    /**
     *  getter for queryParameterWhere
     */
    public String getQueryParameterWhere() {
        return queryParameterWhere;
    }
    
    /**
     *  setter for queryParameterWhere
     */
    public void setQueryParameterWhere(String queryParameterWhere) {
        this.queryParameterWhere = queryParameterWhere;
    }
    
    /**
     *  getter for searchBy
     */
    public String getSearchBy() {
        return searchBy;
    }
    
    
    /**
     *  setter for searchBy
     */
    public void setSearchBy(String searchBy) {
        this.searchBy = searchBy;
    }
    
    /**
     *  setter for split
     */
    public void setSplit(Boolean split) {
        this.split = split;
    }
    
    /**
     *  getter for split
     */
    public Boolean getSplit(){
        return this.split;
    }
}
