// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class PhoneNumberChooser implements ResultHandler {
    
    private final PhoneNumberChooserResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public PhoneNumberChooser(final ResultHandlerConfig config){
        this.config = (PhoneNumberChooserResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

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