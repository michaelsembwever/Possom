// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

/**
 * Search configuration for the Yahoo! media search.
 *
 * @Version $Id$
 */
public class YahooMediaSearchConfiguration extends AbstractYahooSearchConfiguration {

    public final static String DEFAULT_OCR = "yes";
    public final static String DEFAULT_CATALOG = "image"; 

    private String catalog;
    private String ocr;
    private String site;

    /**
     * Creates a new instance of the configuration.
     */
    public YahooMediaSearchConfiguration(){
        super(null);
    }

    /**
     * Creates a new instance of the configuration inheriting values from a parent.
     *
     * @param parent The parent configuration to inherit from.
     */
    public YahooMediaSearchConfiguration(final SearchConfiguration parent){
        super(parent);
        if(parent != null && parent instanceof YahooMediaSearchConfiguration){
            final YahooMediaSearchConfiguration ysc = (YahooMediaSearchConfiguration) parent;
            catalog = ysc.catalog;
            ocr = ysc.ocr;
            site = ysc.site;
        }
    }

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
}
