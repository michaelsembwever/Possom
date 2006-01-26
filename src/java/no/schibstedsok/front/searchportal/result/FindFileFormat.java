package no.schibstedsok.front.searchportal.result;

import java.util.Map;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: itthkjer
 * Date: 21.okt.2005
 * Time: 09:40:46
 * To change this template use File | Settings | File Templates.
 */
public class FindFileFormat implements ResultHandler {
    
    public void handleResult(Context cxt, Map parameters) {
        
        for (Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();
            String url = searchResultItem.getField("url");

            //print out the following fileformats after title
			if (url.endsWith(".pdf"))
				searchResultItem.addField("fileformat", "[pdf]");
            else if (url.endsWith(".doc"))
				searchResultItem.addField("fileformat", "[word]");
            else if (url.endsWith(".ppt"))
				searchResultItem.addField("fileformat", "[power point]");
            else if (url.endsWith(".xls"))
				searchResultItem.addField("fileformat", "[excel]");

        }
    }
}
