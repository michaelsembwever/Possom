// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 * Search configuration for the Yahoo! media search.
 *
 * @version $Id$
 */
@Controller("YahooMediaSearchCommand")
public final class YahooMediaCommandConfig extends AbstractYahooSearchConfiguration {

    /**
     * 
     */
    public final static String DEFAULT_OCR = "yes";
    /**
     * 
     */
    public final static String DEFAULT_CATALOG = "image"; 

    private String catalog;
    private String ocr;
    private String site;

    /**
     * Getter for property 'site'.
     *
     * @return Value for property 'site'.
     */
    public String getSite() {
        return site;
    }

    /**
     * Setter for property 'site'.
     *
     * @param site Value to set for property 'site'.
     */
    public void setSite(final String site) {
        this.site = site;
    }

    /**
     * Getter for property 'catalog'.
     *
     * @return Value for property 'catalog'.
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Setter for property 'catalog'.
     *
     * @param catalog Value to set for property 'catalog'.
     */
    public void setCatalog(final String catalog) {
        this.catalog = catalog;
    }

    /**
     * Getter for property 'ocr'.
     *
     * @return Value for property 'ocr'.
     */
    public String getOcr() {
        return ocr;
    }

    /**
     * Setter for property 'ocr'.
     *
     * @param ocr Value to set for property 'ocr'.
     */
    public void setOcr(final String ocr) {
        this.ocr = ocr;
    }

    @Override
    public AbstractYahooSearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "catalog", ParseType.String, element,
                YahooMediaCommandConfig.DEFAULT_CATALOG);
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "ocr", ParseType.String, element,
                YahooMediaCommandConfig.DEFAULT_OCR);
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "site", ParseType.String, element, "");

        return this;
    }
    
    
}
