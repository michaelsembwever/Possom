/*
 * AdvancedFastConfiguration.java
 *
 * Created on May 30, 2006, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.configuration;

/**
 *
 * @author maek
 */
public class AdvancedFastConfiguration extends AbstractSearchConfiguration {

    private String view;
    private String queryServer;
    private String sortBy;
    
    public AdvancedFastConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    public AdvancedFastConfiguration() {
        super(null);
    }
    
    public String getView() {
        return view;
    }

    public void setView(final String view) {
        this.view = view;
    }

    public String getQueryServer() {
        return queryServer;
    }

    public void setQueryServer(final String queryServer) {
        this.queryServer = queryServer;
    }

    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }
}
