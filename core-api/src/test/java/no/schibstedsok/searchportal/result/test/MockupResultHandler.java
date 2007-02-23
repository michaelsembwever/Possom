package no.schibstedsok.searchportal.result.test;

import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.SearchResult;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupResultHandler implements ResultHandler {
    private SearchResult result;
    private Map parameters;

    public void handleResult(Context cxt, DataModel datamodel) {
        this.result = cxt.getSearchResult();
        this.parameters = parameters;
    }


    public SearchResult getResult() {
        return result;
    }

    public Map getParameters() {
        return parameters;
    }
}
