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
    private boolean clusteringDisabled;

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

    public boolean isClusteringDisabled() {
        return clusteringDisabled;
    }

    public void setClusteringDisabled(boolean clusteringDisabled) {
        this.clusteringDisabled = clusteringDisabled;
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


    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {

        super.readSearchConfiguration(element, inherit);

        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "clusterIdParameter", ParseType.String, element, "clusterId");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "resultsPerCluster", ParseType.Int, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "clusterField", ParseType.String, element, "cluster");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "clusteringDisabled", ParseType.Boolean, element, "false");
        return this;
    }


}
