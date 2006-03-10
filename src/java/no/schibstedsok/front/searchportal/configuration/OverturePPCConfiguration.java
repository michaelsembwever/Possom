// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class OverturePPCConfiguration extends AbstractSearchConfiguration {
    
    private int resultsOnTop;

    public int getResultsOnTop() {
        return resultsOnTop;
    }

    public void setResultsOnTop(int resultsOnTop) {
        this.resultsOnTop = resultsOnTop;
    }
    
}
