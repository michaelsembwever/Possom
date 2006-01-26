package no.schibstedsok.front.searchportal.result;

import java.util.Map;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: itthkjer
 * Date: 24.okt.2005
 * Time: 09:36:39
 * To change this template use File | Settings | File Templates.
 */

public class CategorySplitter implements ResultHandler {
    
    public void handleResult(Context cxt, Map parameters) {
        
        for (Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            String ypbransje = searchResultItem.getField("ypbransje");

            if (ypbransje != null) {
                //splits bransje to show categories under YIP page
                String[] split = ypbransje.split("FASTpbFAST");
                for (int i = 0; i < split.length; i++)
                    searchResultItem.addToMultivaluedField("manyCategories", split[i].trim());
            }

        }
    }
}
