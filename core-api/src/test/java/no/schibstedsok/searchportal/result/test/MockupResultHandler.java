// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import no.schibstedsok.searchportal.result.handler.ResultHandler;
import no.schibstedsok.searchportal.result.ResultList;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class MockupResultHandler implements ResultHandler {
    
    private ResultList<ResultItem> result;
    private Map parameters;

    public void handleResult(final Context cxt, final DataModel datamodel) {
        
        this.result = cxt.getSearchResult();
        this.parameters = parameters;
    }


    public ResultList<ResultItem> getResult() {
        return result;
    }

    public Map getParameters() {
        return parameters;
    }
}
