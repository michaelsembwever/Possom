// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class OverturePPCSearchConfiguration extends AbstractYahooSearchConfiguration {

    public OverturePPCSearchConfiguration(){
        super(null);
    }

    public OverturePPCSearchConfiguration(final SearchConfiguration asc){
        super(asc);
        if(asc != null && asc instanceof OverturePPCSearchConfiguration){
            final OverturePPCSearchConfiguration osc = (OverturePPCSearchConfiguration) asc;
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
