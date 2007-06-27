package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
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
    private String userSortParameter;
    private String sortField;
    private String defaultSort;

    private boolean ignoreOffset = false;
    private String relevanceSortField;


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
     * @return
     */
    public String getDefaultSort() {
        return defaultSort;
    }

    /**
     * @param defaultSort
     */
    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }

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
     * @param mediumPrefix
     */
    public void setMediumPrefix(String mediumPrefix) {
        this.mediumPrefix = mediumPrefix;
    }

    /**
     * @return
     */
    public String getDefaultMedium() {
        return defaultMedium;
    }

    /**
     * @param defaultMedium
     */
    public void setDefaultMedium(String defaultMedium) {
        this.defaultMedium = defaultMedium;
    }

    /**
     * @return
     */
    public String getMediumParameter() {
        return mediumParameter;
    }

    /**
     * @param mediumParameter
     */
    public void setMediumParameter(String mediumParameter) {
        this.mediumParameter = mediumParameter;
    }

    public boolean isIgnoreOffset() {
        return ignoreOffset;
    }

    public void setIgnoreOffset(boolean ignoreOffset) {
        this.ignoreOffset = ignoreOffset;
    }


    public String getRelevanceSortField() {
        return relevanceSortField;
    }

    public void setRelevanceSortField(String relevanceSortField) {
        this.relevanceSortField = relevanceSortField;
    }

    @Override
    public FastCommandConfig readSearchConfiguration(
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
                .fillBeanProperty(this, inherit, "nestedResultsField", ParseType.String, element, "entries");
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "collapsingMaxFetch", ParseType.Int, element, "10");
        AbstractDocumentFactory
                .fillBeanProperty(this, inherit, "ignoreOffset", ParseType.Boolean, element, "false");

        AbstractDocumentFactory.fillBeanProperty(this, inherit, "sortField", ParseType.String, element, "publishedtime");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "defaultSort", ParseType.String, element, "descending");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "userSortParameter", ParseType.String, element, "sort");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "relevanceSortField", ParseType.String, element, "freshnessprofile");
        return this;
    }


}
