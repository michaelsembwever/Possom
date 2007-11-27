/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
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
