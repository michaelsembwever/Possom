package no.schibstedsok.front.searchportal.result;

import no.fast.ds.search.BaseParameter;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PhoneNumberChooser implements ResultHandler {
    public void handleResult(SearchResult result, Map parameters) {
        for (Iterator iterator = result.getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            String phoneNumber = searchResultItem.getField("yptelefon");
            String otherNumbers = searchResultItem.getField("ypandretelefoner");
            String mobileNumber = searchResultItem.getField("ypmobiltelefon");

            String chosenNumber = null;

            if (phoneNumber != null) {
                chosenNumber = phoneNumber;
            } else {
                if (otherNumbers != null) {
                    String[] numbers = otherNumbers.split(";");

                    if (numbers.length > 0) {
                        chosenNumber = numbers[0];
                    }
                } else {
                    chosenNumber = mobileNumber;
                }
            }

            searchResultItem.addField("ypanynumber", chosenNumber);
        }
    }
}