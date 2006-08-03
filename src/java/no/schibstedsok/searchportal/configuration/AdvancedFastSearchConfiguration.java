/*
 * AdvancedFastConfiguration.java
 *
 * Created on May 30, 2006, 4:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author maek
 */
public class AdvancedFastSearchConfiguration extends AbstractSearchConfiguration {

    private String view;
    private String queryServer;
    private String sortBy;
    private boolean collapsingEnabled;
    private String collapseOnField;
    private String qtPipeline;
    
    public void setCollapseOnField(String collapseOnField) {
        this.collapseOnField = collapseOnField;
    }

    public String getCollapseOnField() {
        return collapseOnField;
    }
    
    public void setCollapsingEnabled(final boolean collapsingEnabled) {
        this.collapsingEnabled = collapsingEnabled;
    }

    public boolean isCollapsingEnabled() {
        return collapsingEnabled;
    }
    
    private final Map<String,FastNavigator> navigators = new HashMap<String,FastNavigator>();

    public AdvancedFastSearchConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    public AdvancedFastSearchConfiguration() {
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
    
    public Map<String,FastNavigator> getNavigators() {
        return navigators;
    }
    public void addNavigator(final FastNavigator navigator, final String navKey) {
        navigators.put(navKey, navigator);
    }

    public FastNavigator getNavigator(final String navigatorKey) {
        return navigators.get(navigatorKey);
    }

    public void setQtPipeline(final String qtPipeline) {
        this.qtPipeline = qtPipeline;
    }

    public String getQtPipeline() {
        return qtPipeline;
    }
}