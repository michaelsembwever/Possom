/*
 * Copyright (2005) Schibsted Søk AS
 *
 */
package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.executor.SearchCommandExecutor;
import no.schibstedsok.front.searchportal.executor.SequentialSearchCommandExecutor;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchMode {

    private SearchCommandExecutor searchCommandExecutor = new SequentialSearchCommandExecutor();
    private String key;

    private Collection searchConfigurations = new ArrayList();
    private String parentMode;
    private boolean queryAnalysisEnabled = false;
    
    public String getKey() {
        if (parentMode != null) {
            return parentMode;
        } else {
            return key;
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Collection getSearchConfigurations() {
        return searchConfigurations;
    }

    public void setSearchConfigurations(Collection searchConfigurations) {
        this.searchConfigurations = searchConfigurations;
    }

    public void addSearchConfiguration(SearchConfiguration conf) {
        searchConfigurations.add(conf);
    }


    public SearchCommandExecutor getExecutor() {
        return searchCommandExecutor;
    }

    public void setExecutor(SearchCommandExecutor searchCommandExecutor) {
        this.searchCommandExecutor = searchCommandExecutor;
    }

    public String getParentMode() {
        return parentMode;
    }

    public void setParentMode(String parentMode) {
        this.parentMode = parentMode;
    }

    /**
     * Get the queryAnalysisEnabled.
     *
     * @return the queryAnalysisEnabled.
     */
    public boolean isQueryAnalysisEnabled() {
        return queryAnalysisEnabled;
    }

    /**
     * Set the queryAnalysisEnabled.
     *
     * @param queryAnalysisEnabled The queryAnalysisEnabled to set.
     */
    public void setQueryAnalysisEnabled(boolean queryAnalysisEnabled) {
        this.queryAnalysisEnabled = queryAnalysisEnabled;
    }
}
