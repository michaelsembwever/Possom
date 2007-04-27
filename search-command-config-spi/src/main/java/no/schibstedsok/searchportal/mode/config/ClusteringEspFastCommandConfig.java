// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;

/**
 * @author mick
 * @version $Id$
 */
@Controller("ClusteringESPFastCommand")
public class ClusteringEspFastCommandConfig extends NewsEspCommandConfig {

    private String clusterIdParameter = "clusterId";
    private int resultsPerCluster;
    private String clusterField;
    private String clusterMaxFetch;
    private String nestedResultsField;
    private String userSortParameter;
    private String sortField;
    private String defaultSort;

    public ClusteringEspFastCommandConfig(){}
    
    /**
     * @param asc
     */
    public ClusteringEspFastCommandConfig(SearchConfiguration asc) {

        if (asc instanceof ClusteringEspFastCommandConfig) {
            ClusteringEspFastCommandConfig cefcc = (ClusteringEspFastCommandConfig) asc;
            clusterIdParameter = cefcc.getClusterIdParameter();
            resultsPerCluster = cefcc.getResultsPerCluster();
            clusterField = cefcc.getClusterField();
            clusterMaxFetch = cefcc.getClusterMaxFetch();
            nestedResultsField = cefcc.getNestedResultsField();
            userSortParameter = cefcc.getUserSortParameter();
            sortField = cefcc.getSortField();
        }
    }

    /**
     * @return
     */
    public String getUserSortParameter() {
        return userSortParameter;
    }

    /**
     * @param userSortParameter
     */
    public void setUserSortParameter(String userSortParameter) {
        this.userSortParameter = userSortParameter;
    }

    /**
     * @return
     */
    public String getNestedResultsField() {
        return nestedResultsField;
    }

    /**
     * @param nestedResultsField
     */
    public void setNestedResultsField(String nestedResultsField) {
        this.nestedResultsField = nestedResultsField;
    }

    /**
     * @return
     */
    public String getClusterIdParameter() {
        return clusterIdParameter;
    }

    /**
     * @param clusterIdParameter
     */
    public void setClusterIdParameter(String clusterIdParameter) {
        this.clusterIdParameter = clusterIdParameter;
    }

    /**
     * @param resultsPerCluster
     */
    public void setResultsPerCluster(int resultsPerCluster) {
        this.resultsPerCluster = resultsPerCluster;
    }

    /**
     * @return
     */
    public int getResultsPerCluster() {
        return resultsPerCluster;
    }

    /**
     * @return
     */
    public String getClusterField() {
        return clusterField;
    }

    /**
     * @param clusterField
     */
    public void setClusterField(String clusterField) {
        this.clusterField = clusterField;
    }

    /**
     * @return
     */
    public String getClusterMaxFetch() {
        return clusterMaxFetch;
    }

    /**
     * @param clusterMaxFetch
     */
    public void setClusterMaxFetch(String clusterMaxFetch) {
        this.clusterMaxFetch = clusterMaxFetch;
    }

    /**
     * @return
     */
    public String getSortField() {
        return sortField;
    }

    /**
     * @param sortField
     */
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }


    public String getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }
}
