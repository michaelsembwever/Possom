// Copyright (2007) Schibsted Søk AS
/*
 * AdvancedFastConfiguration.java
 *
 * Created on May 30, 2006, 4:16 PM
 *
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.Collection;
import no.schibstedsok.searchportal.result.Navigator;

import java.util.HashMap;
import java.util.Map;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author maek
 * @version $Id$
 */
@Controller("ESPFastSearchCommand")
public class EspFastCommandConfig extends CommandConfig {

    // Constants -----------------------------------------------------
    
    private String view;
    private String queryServer;
    private String sortBy;
    private boolean collapsingEnabled;
    private boolean expansionEnabled;
    private boolean collapsingRemoves;
    private String qtPipeline;
    

    private static final String ERR_FAST_EPS_QR_SERVER =
            "Query server address cannot contain the scheme (http://): ";
    

    /**
     * 
     * @param collapsingEnabled 
     */
    public void setCollapsingEnabled(final boolean collapsingEnabled) {
        this.collapsingEnabled = collapsingEnabled;
    }

    /**
     * 
     * @return 
     */
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

    /**
     * 
     * @return 
     */
    public boolean isCollapsingRemoves() {
        return collapsingRemoves;
    }

    /**
     * 
     * @param collapsingRemoves 
     */
    public void setCollapsingRemoves(final boolean collapsingRemoves) {
        this.collapsingRemoves = collapsingRemoves;
    }

    private final Map<String, Navigator> navigators = new HashMap<String,Navigator>();

    /**
     * 
     */
    public EspFastCommandConfig() {
    }
    
    /**
     * 
     * @return 
     */
    public String getView() {
        return view;
    }

    /**
     * 
     * @param view 
     */
    public void setView(final String view) {
        this.view = view;
    }

    /**
     * 
     * @return 
     */
    public String getQueryServer() {
        return queryServer;
    }

    /**
     * 
     * @param queryServer 
     */
    public void setQueryServer(final String queryServer) {
        this.queryServer = queryServer;
    }

    /**
     * 
     * @return 
     */
    public String getSortBy() {
        return sortBy;
    }
    
    /**
     * 
     * @param sortBy 
     */
    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy;
    }
    
    /**
     * 
     * @return 
     */
    public Map<String, Navigator> getNavigators() {
        return navigators;
    }
    /**
     * 
     * @param navigator 
     * @param navKey 
     */
    public void addNavigator(final Navigator navigator, final String navKey) {
        navigators.put(navKey, navigator);
    }

    /**
     * 
     * @param navigatorKey 
     * @return 
     */
    public Navigator getNavigator(final String navigatorKey) {
        return navigators.get(navigatorKey);
    }

    /**
     * 
     * @param qtPipeline 
     */
    public void setQtPipeline(final String qtPipeline) {
        this.qtPipeline = qtPipeline;
    }

    /**
     * 
     * @return 
     */
    public String getQtPipeline() {
        return qtPipeline;
    }

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        
        final EspFastCommandConfig ascInherit = inherit instanceof EspFastCommandConfig
                ? (EspFastCommandConfig) inherit
                : null;

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "view", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "sortBy", ParseType.String, element, "default");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "collapsingRemoves", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "collapsingEnabled", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "expansionEnabled", ParseType.Boolean, element, "false");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "qtPipeline", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "queryServer", ParseType.String, element, "");

        if (null != getQueryServer() && getQueryServer().startsWith("http://")) {
            throw new IllegalArgumentException(ERR_FAST_EPS_QR_SERVER + getQueryServer());
        }

        // navigators
        if (ascInherit != null && ascInherit.getNavigators() != null) {
            
            navigators.putAll(ascInherit.getNavigators());
        }
        
        final NodeList nList = element.getElementsByTagName("navigators");
        for (int i = 0; i < nList.getLength(); ++i) {
            final Collection<Navigator> navigators = parseNavigators((Element) nList.item(i));
            for (Navigator navigator : navigators) {
                addNavigator(navigator, navigator.getId());
            }
        }

        return this;
    }
    
    
}
