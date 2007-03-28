// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

public class ClusteringESPFastConfiguration extends NavigatableESPFastConfiguration {
    private String clusterIdParameter = "clusterId";
    private int resultsPerCluster;
    private String clusterField;
    private String clusterMaxFetch;
    private String nestedResultsField;

    public ClusteringESPFastConfiguration(SearchConfiguration asc) {
        super(asc);
        if (asc instanceof ClusteringESPFastConfiguration) {
            ClusteringESPFastConfiguration cefcc = (ClusteringESPFastConfiguration) asc;
            clusterIdParameter = cefcc.getClusterIdParameter();
            resultsPerCluster = cefcc.getResultsPerCluster();
            clusterField = cefcc.getClusterField();
            clusterMaxFetch = cefcc.getClusterMaxFetch();
            nestedResultsField = cefcc.getNestedResultsField();
        }
    }

    public String getNestedResultsField() {
        return nestedResultsField;
    }

    public void setNestedResultsField(String nestedResultsField) {
        this.nestedResultsField = nestedResultsField;
    }

    public String getClusterIdParameter() {
        return clusterIdParameter;
    }

    public void setClusterIdParameter(String clusterIdParameter) {
        this.clusterIdParameter = clusterIdParameter;
    }

    public void setResultsPerCluster(int resultsPerCluster) {
        this.resultsPerCluster = resultsPerCluster;
    }

    public int getResultsPerCluster() {
        return resultsPerCluster;
    }

    public String getClusterField() {
        return clusterField;
    }

    public void setClusterField(String clusterField) {
        this.clusterField = clusterField;
    }

    public String getClusterMaxFetch() {
        return clusterMaxFetch;
    }

    public void setClusterMaxFetch(String clusterMaxFetch) {
        this.clusterMaxFetch = clusterMaxFetch;
    }
}
