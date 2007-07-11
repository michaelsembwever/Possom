/*
 * DataModelWrapper.java
 * 
 * Created on 27-Jun-2007, 12:33:59
 */

package no.schibstedsok.searchportal.http.dwr;

import java.util.List;
import javax.servlet.http.HttpSession;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.datamodel.request.BrowserDataObject;
import no.schibstedsok.searchportal.datamodel.search.SearchDataObject;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
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
