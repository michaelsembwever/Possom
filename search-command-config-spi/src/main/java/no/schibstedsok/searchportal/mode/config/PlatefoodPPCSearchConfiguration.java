// Copyright (2007) Schibsted Søk AS
/*
 * PlatefoodPPCSearchConfiguration.java
 *
 * Created on 24. august 2006, 10:06
 */

package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 *
 * @author SSTHKJER
 * @version $Id$
 */
@Controller("PlatefoodPPCSearchCommand")
public final class PlatefoodPPCSearchConfiguration extends AbstractYahooSearchConfiguration {
       
    /**
     * 
     */
    public PlatefoodPPCSearchConfiguration(){
        super(null);
    }    

    /**
     * 
     * @param asc 
     */
    public PlatefoodPPCSearchConfiguration(final SearchConfiguration asc){
        super(asc);
        if(asc != null && asc instanceof PlatefoodPPCSearchConfiguration){
            final PlatefoodPPCSearchConfiguration osc = (PlatefoodPPCSearchConfiguration) asc;
            resultsOnTop = osc.resultsOnTop;
            url = osc.url;
        }
    }



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
    
}
