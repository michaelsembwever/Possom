package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.OverturePPCCommand;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class OverturePPCConfiguration extends AbstractSearchConfiguration {

    private static final String DEFAULT_PARTNER_ID = "schibstedsok_xml_no_searchbox_imp1";

    private int resultsOnTop;

    private String partnerId;

    public int getResultsOnTop() {
        return resultsOnTop;
    }

    public void setResultsOnTop(int resultsOnTop) {
        this.resultsOnTop = resultsOnTop;
    }

    public String getPartnerId() {
        return partnerId == null ? DEFAULT_PARTNER_ID : partnerId;
    }
}
