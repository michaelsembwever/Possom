// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class OverturePPCConfiguration extends AbstractYahooSearchConfiguration {

    public OverturePPCConfiguration(){
        super(null);
    }

    public OverturePPCConfiguration(final SearchConfiguration asc){
        super(asc);
        if(asc != null && asc instanceof OverturePPCConfiguration){
            final OverturePPCConfiguration osc = (OverturePPCConfiguration) asc;
            resultsOnTop = osc.resultsOnTop;
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

}
