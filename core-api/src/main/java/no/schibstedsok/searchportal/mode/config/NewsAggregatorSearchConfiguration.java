// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import org.apache.log4j.Logger;

public class NewsAggregatorSearchConfiguration extends NavigatableESPFastConfiguration {
    private final static Logger log = Logger.getLogger(NewsAggregatorSearchConfiguration.class);

    private String xmlSource;
    private String xmlMainFile;
    private String clusterField;
    private String nestedResultsField;
    private int relatedMaxCount = 30;
    private int resultsPerCluster;
    private int clusterMaxFetch;

    public NewsAggregatorSearchConfiguration() {
        super(null);
    }

    public NewsAggregatorSearchConfiguration(SearchConfiguration sc) {
        super(sc);
        if (sc instanceof NewsAggregatorSearchConfiguration) {
            final NewsAggregatorSearchConfiguration nasc = (NewsAggregatorSearchConfiguration) sc;
            nasc.setXmlSource(nasc.getXmlSource());
            nasc.setXmlMainFile(nasc.getXmlMainFile());
            nasc.setClusterField(nasc.getClusterField());
            nasc.setNestedResultsField(nasc.getNestedResultsField());
            nasc.setRelatedMaxCount(nasc.getRelatedMaxCount());
            nasc.setResultsPerCluster(nasc.getResultsPerCluster());
            nasc.setClusterMaxFetch(nasc.getClusterMaxFetch());
        }
    }

    public int getRelatedMaxCount() {
        return relatedMaxCount;
    }

    public void setRelatedMaxCount(int relatedMaxCount) {
        this.relatedMaxCount = relatedMaxCount;
    }

    public String getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(String xmlSource) {
        this.xmlSource = xmlSource;
    }

    public void setXmlMainFile(String xmlMainFile) {
        this.xmlMainFile = xmlMainFile;
    }

    public String getXmlMainFile() {
        return xmlMainFile;
    }

    public String getClusterField() {
        return clusterField;
    }

    public void setClusterField(String clusterField) {
        this.clusterField = clusterField;
    }

    public String getNestedResultsField() {
        return nestedResultsField;
    }

    public void setNestedResultsField(String nestedResultsField) {
        this.nestedResultsField = nestedResultsField;
    }

    public void setResultsPerCluster(int resultsPerCluster) {
        this.resultsPerCluster = resultsPerCluster;
    }

    public int getResultsPerCluster() {
        return resultsPerCluster;
    }


    public int getClusterMaxFetch() {
        return clusterMaxFetch;
    }

    public void setClusterMaxFetch(int clusterMaxFetch) {
        this.clusterMaxFetch = clusterMaxFetch;
    }
}
