// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PhoneNumberChooser implements ResultHandler {

    public void handleResult(final Context cxt, final Map parameters) {

        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            final String phoneNumber = searchResultItem.getField("yptelefon");
            final String otherNumbers = searchResultItem.getField("ypandretelefoner");
            final String mobileNumber = searchResultItem.getField("ypmobiltelefon");

            String chosenNumber = null;

            if (phoneNumber != null) {
                chosenNumber = phoneNumber;
            } else {
                if (otherNumbers != null) {
                    final String[]  numbers = otherNumbers.split(";");

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