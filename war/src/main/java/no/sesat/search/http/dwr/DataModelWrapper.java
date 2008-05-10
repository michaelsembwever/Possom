/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
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
 *
 * DataModelWrapper.java
 *
 * Created on 27-Jun-2007, 12:33:59
 */

package no.sesat.search.http.dwr;

import java.util.List;
import javax.servlet.http.HttpSession;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.request.BrowserDataObject;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class DataModelWrapper {

    private static Logger LOG = Logger.getLogger(DataModelWrapper.class);

    private DataModelWrapper() {
    }

    public static DataModel getDataModel() {

        HttpSession session = null;
//  Uncomment the following two codelines to enable DWR.
//    The dwr dependency in the pom.xml and servlet in the web.xml will also need to be uncommented.
//        final org.directwebremoting.WebContext webContext = org.directwebremoting.WebContextFactory.get();
//        session = webContext.getSession(false);

        return null == session ? createDefaultDataModel() : (DataModel) session.getAttribute(DataModel.KEY);
    }

    public static BrowserDataObject getBrowser() {
        final DataModel datamodel = getDataModel();
        return datamodel.getBrowser();
    }

    public static SearchDataObject getSearch(final String name) {
        final DataModel datamodel = getDataModel();
        final SearchDataObject searchDO = datamodel.getSearch(name);
        return searchDO;
    }

    public static ResultList<ResultItem> getResults(final String name) {
        final DataModel datamodel = getDataModel();
        final SearchDataObject searchDO = datamodel.getSearch(name);
        if (searchDO == null) {
            return null;
        }
        final ResultList<ResultItem> results = searchDO.getResults();
        return results;
    }

    public static ResultItem getResultItem(final String name, final int index) {
        final ResultList<ResultItem> resultlist= getResults(name);
        final List<ResultItem> results = resultlist.getResults();
        if (results.size() > (index - 1)) {
            return results.get(index - 1);
        }
        return null;
    }

    private static DataModel createDefaultDataModel() {
       return null;
    }
}
