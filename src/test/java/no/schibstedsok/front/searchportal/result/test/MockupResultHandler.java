package no.schibstedsok.front.searchportal.result.test;

import no.schibstedsok.front.searchportal.result.ResultHandler;
import no.schibstedsok.front.searchportal.result.SearchResult;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupResultHandler implements ResultHandler {
    private SearchResult result;
    private Map parameters;

    public void handleResult(SearchResult result, Map parameters) {
        this.result = result;
        this.parameters = parameters;
    }


    public SearchResult getResult() {
        return result;
    }

    public Map getParameters() {
        return parameters;
    }
}
