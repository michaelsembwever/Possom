/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.result.test;

import no.sesat.search.result.handler.ResultHandler;
import no.sesat.search.result.ResultList;
import java.util.Map;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;

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
