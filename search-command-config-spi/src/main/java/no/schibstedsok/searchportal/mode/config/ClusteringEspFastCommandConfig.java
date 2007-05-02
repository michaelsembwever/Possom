// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * @author geir
 * @version $Id$
 */
@Controller("ClusteringESPFastCommand")
public class ClusteringEspFastCommandConfig extends NewsEspCommandConfig {

    private String clusterIdParameter = "clusterId";
    private int resultsPerCluster;
    private String clusterField;
    private String userSortParameter;
    private String sortField;
    private String defaultSort;

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
    public String getSortField() {
        return sortField;
    }

    /**
     * @param sortField
     */
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }


    /**
     * 
     * @return 
     */
    public String getDefaultSort() {
        return defaultSort;
    }

    /**
     * 
     * @param defaultSort 
     */
    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "clusterIdParameter", ParseType.String, element, "clusterId");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "resultsPerCluster", ParseType.Int, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "clusterField", ParseType.String, element, "cluster");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "clusterMaxFetch", ParseType.Int, element, "10");
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "nestedResultsField", ParseType.String, element, "entries");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "sortField", ParseType.String, element, "publishedtime");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "defaultSort", ParseType.String, element, "descending");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "userSortParameter", ParseType.String, element, "sort");

        return this;
    }
    
    
}
