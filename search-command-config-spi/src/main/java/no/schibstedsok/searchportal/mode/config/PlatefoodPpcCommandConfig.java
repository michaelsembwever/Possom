// Copyright (2007) Schibsted Søk AS
/*
 * PlatefoodPPCCommandConfig.java
 *
 * Created on 24. august 2006, 10:06
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory;
import no.schibstedsok.searchportal.site.config.AbstractDocumentFactory.ParseType;
import org.w3c.dom.Element;

/**
 *
 * @author SSTHKJER
 * @version $Id$
 */
@Controller("PlatefoodPPCSearchCommand")
public final class PlatefoodPpcCommandConfig extends AbstractYahooSearchConfiguration {

    private int resultsOnTop;


    /** @deprecated use views.xml instead **/
    public int getResultsOnTop() {
        return resultsOnTop;
    }

    /** @deprecated use views.xml instead **/
    public void setResultsOnTop(final int resultsOnTop) {
        this.resultsOnTop = resultsOnTop;
    }

    /**
     * Holds value of property url.
     */
    private String url;

    /**
     * Getter for property url.
     *
     * @return Value of property url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Setter for property url.
     *
     * @param url New value of property url.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public AbstractYahooSearchConfiguration readSearchConfiguration(
            final Element element,
            final SearchConfiguration inherit) {
        
        super.readSearchConfiguration(element, inherit);
        
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "url", ParseType.String, element, "");
        AbstractDocumentFactory.fillBeanProperty(this, inherit, "resultsOnTop", ParseType.String, element, "");
                    
        return this;
    }
    
    
}
