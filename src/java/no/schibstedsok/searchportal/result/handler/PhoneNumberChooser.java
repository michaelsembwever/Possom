// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Map;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PhoneNumberChooser implements ResultHandler {

    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String phoneNumber = item.getField("yphovedtelefon");
            final String otherNumbers = item.getField("ypandretelefoner");
            final String mobileNumber = item.getField("ypmobiltelefon");

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

            item.addField("ypanynumber", chosenNumber);
        }
    }
}