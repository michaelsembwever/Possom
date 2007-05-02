// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * @author mick
 * @version <tt>$Id$</tt>
 */
@Controller("YahooIdpSearchCommand")
public final class YahooIdpCommandConfig extends AbstractYahooSearchConfiguration {

    /**
     * Holds value of property database.
     */
    private String database;

    /**
     * Getter for property database.
     * @return Value of property database.
     */
    public String getDatabase() {
        return this.database;
    }

    /**
     * Setter for property database.
     * @param database New value of property database.
     */
    public void setDatabase(final String database) {
        this.database = database;
    }

    /**
     * Holds value of property dateRange.
     */
    private String dateRange;

    /**
     * Getter for property dateRange.
     * @return Value of property dateRange.
     */
    public String getDateRange() {
        return this.dateRange;
    }

    /**
     * Setter for property dateRange.
     * @param dateRange New value of property dateRange.
     */
    public void setDateRange(final String dateRange) {
        this.dateRange = dateRange;
    }

    /**
     * Holds value of property spellState.
     */
    private String spellState;

    /**
     * Getter for property spellstate.
     * @return Value of property spellstate.
     */
    public String getSpellState() {
        return this.spellState;
    }

    /**
     * Setter for property spellstate.
     * @param spellState New value of property spellstate.
     */
    public void setSpellState(final String spellState) {
        this.spellState = spellState;
    }

    /**
     * Holds value of property regionMix.
     */
    private String regionMix;

    /**
     * Getter for property regionMix.
     * @return Value of property regionMix.
     */
    public String getRegionMix() {
        return this.regionMix;
    }

    /**
     * Setter for property regionMix.
     * @param regionMix New value of property regionMix.
     */
    public void setRegionMix(final String regionMix) {
        this.regionMix = regionMix;
    }

    /**
     * Holds value of property unique.
     */
    private String unique;

    /**
     * Getter for property unique.
     * @return Value of property unique.
     */
    public String getUnique() {
        return this.unique;
    }

    /**
     * Setter for property unique.
     * @param unique New value of property unique.
     */
    public void setUnique(final String unique) {
        this.unique = unique;
    }

    /**
     * Holds value of property region.
     */
    private String region;

    /**
     * Getter for property region.
     * @return Value of property region.
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * Setter for property region.
     * @param region New value of property region.
     */
    public void setRegion(final String region) {
        this.region = region;
    }

    /**
     * Holds value of property filter.
     */
    private String filter;

    /**
     * Getter for property filter.
     * @return Value of property filter.
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * Setter for property filter.
     * @param filter New value of property filter.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * Holds value of property hideDomain.
     */
    private String hideDomain;

    /**
     * Getter for property hideDomain.
     * @return Value of property hideDomain.
     */
    public String getHideDomain() {
        return this.hideDomain;
    }

    /**
     * Setter for property hideDomain.
     * Because Yahoo IDP does not supply domain exclusion without ruining the relevance.
     * @param hideDomain New value of property hideDomain.
     */
    public void setHideDomain(final String hideDomain) {
        this.hideDomain = hideDomain;
    }

    /**
     * Holds value of property language.
     */
    private String language;

    /**
     * Getter for property language.
     * @return Value of property language.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Setter for property language.
     * @param language New value of property language.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Holds value of property languageMix.
     */
    private String languageMix;

    /**
     * Getter for property languageMix.
     * @return Value of property languageMix.
     */
    public String getLanguageMix() {
        return this.languageMix;
    }

    /**
     * Setter for property languageMix.
     * @param languageMix New value of property languageMix.
     */
    public void setLanguageMix(String languageMix) {
        this.languageMix = languageMix;
    }

    @Override
    public AbstractYahooSearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "database", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "dateRange", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "filter", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "hideDomain", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "language", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "languageMix", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "region", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "regionMix", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "spellState", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "unique", ParseType.String, element, "");

        return this;
    }



}
