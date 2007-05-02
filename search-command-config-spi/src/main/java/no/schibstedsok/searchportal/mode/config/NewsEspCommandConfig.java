package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * 
 * @author geir
 * @version $Id$
 */
@Controller("NewsEspSearchCommand")
public class NewsEspCommandConfig extends NavigatableEspFastCommandConfig {
    /**
     * 
     */
    public static final String ALL_MEDIUMS = "all";
    private String mediumPrefix = "medium";
    private String defaultMedium = "webnewsarticle";
    private String mediumParameter = "medium";
    private String nestedResultsField;
    private int collapsingMaxFetch;
    private boolean ignoreOffset = false;

    /**
     * @return
     */
    public int getCollapsingMaxFetch() {
        return collapsingMaxFetch;
    }

    /**
     * @param collapsingMaxFetch
     */
    public void setCollapsingMaxFetch(int collapsingMaxFetch) {
        this.collapsingMaxFetch = collapsingMaxFetch;
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

    public String getMediumPrefix() {
        return mediumPrefix;
    }

    /**
     * 
     * @param mediumPrefix 
     */
    public void setMediumPrefix(String mediumPrefix) {
        this.mediumPrefix = mediumPrefix;
    }

    /**
     * 
     * @return 
     */
    public String getDefaultMedium() {
        return defaultMedium;
    }

    /**
     * 
     * @param defaultMedium 
     */
    public void setDefaultMedium(String defaultMedium) {
        this.defaultMedium = defaultMedium;
    }

    /**
     * 
     * @return 
     */
    public String getMediumParameter() {
        return mediumParameter;
    }

    /**
     * 
     * @param mediumParameter 
     */
    public void setMediumParameter(String mediumParameter) {
        this.mediumParameter = mediumParameter;
    }

    @Override
    public CommandConfig readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "mediumPrefix", ParseType.String, element, "medium");
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "defaultMedium", ParseType.String, element, "webnewsarticle");
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "mediumParameter", ParseType.String, element, "medium");
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "ignoreOffset", ParseType.Boolean, element, "false");
        
        return this;
    }
    
    
    public boolean isIgnoreOffset() {
        return ignoreOffset;
    }

    public void setIgnoreOffset(boolean ignoreOffset) {
        this.ignoreOffset = ignoreOffset;
    }


}
