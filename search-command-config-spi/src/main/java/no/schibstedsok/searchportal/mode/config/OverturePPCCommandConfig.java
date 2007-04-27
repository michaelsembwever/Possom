// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import no.schibstedsok.searchportal.mode.config.AbstractSearchConfiguration.Controller;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
@Controller("OverturePPCSearchCommand")
public final class OverturePPCSearchConfiguration extends AbstractYahooSearchConfiguration {

    /**
     * 
     */
    public OverturePPCSearchConfiguration(){
        super(null);
    }

    /**
     * 
     * @param asc 
     */
    public OverturePPCSearchConfiguration(final SearchConfiguration asc){
        super(asc);
        if(asc != null && asc instanceof OverturePPCSearchConfiguration){
            final OverturePPCSearchConfiguration osc = (OverturePPCSearchConfiguration) asc;
//            resultsOnTop = osc.resultsOnTop;
            url = osc.url;
            type = osc.type;
        }
    }



//    private int resultsOnTop;
//
//    /** @deprecated use views.xml instead **/
//    public int getResultsOnTop() {
//        return resultsOnTop;
//    }
//
//    /** @deprecated use views.xml instead **/
//    public void setResultsOnTop(final int resultsOnTop) {
//        this.resultsOnTop = resultsOnTop;
//    }

    /**
     * Holds value of property url.
     */
    private String url;
    
    private String type;

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

    
    /**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Setter for property type.
     *
     * @param type New value of property type.
     */
    public void setType(final String type) {
        this.type = type;
    }

    
}
