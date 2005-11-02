package no.schibstedsok.front.searchportal.result;

import java.util.Map;
import java.util.Iterator;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TextOutputResultHandler implements ResultHandler {
    public void handleResult(SearchResult result, Map parameters) {
        for (Iterator iterator = result.getResults().iterator(); iterator.hasNext();) {
            BasicSearchResultItem basicSearchResultItem = (BasicSearchResultItem) iterator.next();

            for (Iterator iterator1 = basicSearchResultItem.getFieldNames().iterator(); iterator1.hasNext();) {
                String name =  (String) iterator1.next();
                System.out.println(name + " => " + basicSearchResultItem.getField(name));
            }

            System.out.println();
        }
    }
}
