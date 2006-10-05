/*
 * AdvancedFastConfiguration.java
 *
 * Created on May 30, 2006, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.result.Navigator;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author maek
 */
public class ESPFastSearchConfiguration extends AbstractSearchConfiguration {

    private String view;
    private String queryServer;
    private String sortBy;
    private boolean collapsingEnabled;
    private boolean expansionEnabled;
    private String qtPipeline;
    
    public void setCollapsingEnabled(final boolean collapsingEnabled) {
        this.collapsingEnabled = collapsingEnabled;
    }

    public boolean isCollapsingEnabled() {
        return collapsingEnabled;
    }

    /**
     * Returns true if expansion is enabled. Expansion means the possibility 
     * to retrieve all of the documents that has been collapsed for a domain. If
     * this is set to false the templates won't get the information that there
     * are collapsed documents.
     *
     * @return true if expansion is enabled.
     */
    public boolean isExpansionEnabled() {
        return expansionEnabled;
    }

    /**
     * Setter for the expansionEnabled property.
     *
     * @param expansionEnabled 
     */
    public void setExpansionEnabled(final boolean expansionEnabled) {
        this.expansionEnabled = expansionEnabled;
    }

    private final Map<String, Navigator> navigators = new HashMap<String,Navigator>();

    public ESPFastSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    public ESPFastSearchConfiguration() {
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
    
    public Map<String, Navigator> getNavigators() {
        return navigators;
    }
    public void addNavigator(final Navigator navigator, final String navKey) {
        navigators.put(navKey, navigator);
    }

    public Navigator getNavigator(final String navigatorKey) {
        return navigators.get(navigatorKey);
    }

    public void setQtPipeline(final String qtPipeline) {
        this.qtPipeline = qtPipeline;
    }

    public String getQtPipeline() {
        return qtPipeline;
    }
}