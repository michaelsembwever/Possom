/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
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
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

/**
 *
 * @author andersjj
 */
public class DataModelWrapper {

    private static Logger LOG = Logger.getLogger(DataModelWrapper.class);
    
    private DataModelWrapper() {
    }
    
    public static DataModel getDataModel() {
       
        final WebContext webContext = WebContextFactory.get();
        final HttpSession session = webContext.getSession(false);
        
        if (session == null) {
            return createDefaultDataModel();
        }
       
        final DataModel datamodel = (DataModel) session.getAttribute(DataModel.KEY);
        
        return datamodel;
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
