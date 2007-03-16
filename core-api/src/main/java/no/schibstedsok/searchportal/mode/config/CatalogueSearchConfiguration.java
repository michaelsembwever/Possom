/*
 * Copyright (2005-2007) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.mode.config;

/**
 *
 * An implementation of Search Configuration for yellow searches.
 *
 * @author <a href="larsj@conduct.no">Lars Johansson</a>
 * @version $Revision: 1 $
 */
public class CatalogueSearchConfiguration extends FastSearchConfiguration {

    public CatalogueSearchConfiguration(){
        super(null);
    }

    public CatalogueSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    
    private String queryParameterWhere;
    private String searchBy;
    private Boolean split;
    
    
    public String getQueryParameterWhere() {
            return queryParameterWhere;
    }

    public void setQueryParameterWhere(String queryParameterWhere) {
            this.queryParameterWhere = queryParameterWhere;
    }

    public String getSearchBy() {
            return searchBy;
    }

    public void setSearchBy(String searchBy) {
            this.searchBy = searchBy;
    }

    public void setSplit(Boolean split) {
            this.split = split;
    }

    public Boolean getSplit(){
        return this.split;
    }
}
