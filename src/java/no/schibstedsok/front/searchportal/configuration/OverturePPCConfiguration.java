// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class OverturePPCConfiguration extends AbstractSearchConfiguration {

    public OverturePPCConfiguration(){
        super(null);
    }

    public OverturePPCConfiguration(final SearchConfiguration asc){
        super(asc);
        if(asc != null && asc instanceof OverturePPCConfiguration){
            final OverturePPCConfiguration osc = (OverturePPCConfiguration) asc;
            partnerId = osc.partnerId;
        }
    }

    private static final String DEFAULT_PARTNER_ID = "schibstedsok_xml_no_searchbox_imp1";

    private int resultsOnTop;

    private String partnerId;

    public int getResultsOnTop() {
        return resultsOnTop;
    }

    public void setResultsOnTop(final int resultsOnTop) {
        this.resultsOnTop = resultsOnTop;
    }

    public String getPartnerId() {
        return partnerId == null ? DEFAULT_PARTNER_ID : partnerId;
    }

    /**
     * Setter for property partnerId.
     * @param partnerId New value of property partnerId.
     */
    public void setPartnerId(final java.lang.String partner) {
        partnerId = partner; 
    }
}
