// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * @author <a href="mailto:thomas.kjerstad@aftenposten.no">Thomas Kjærstad</a>
 * @version <tt>$Revision$</tt>
 */
public class PhoneNumberFormatter implements ResultHandler {

    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String ypanynumber = item.getField("ypanynumber");
            final String wpmobiltelefon = item.getField("wpmobiltelefon");
            final String wptelefon = item.getField("wptelefon");
            final String yptelefax = item.getField("yptelefax");
            String format = null;

            // TODO: put formatting oh white numbers in here too. For now this will be formatted in MultiValueFieldCollector
            //formatting wp mobile numbers in enrichment
            if (wpmobiltelefon != null) {
                format = wpmobiltelefon.substring(0, 2) + " " + wpmobiltelefon.substring(2, 4) + " " + wpmobiltelefon.substring(4, 6) + " " + wpmobiltelefon.substring(6, 8);
                item.addField("wpmobiltelefon", format);
            }

            //formatting wptelefon in enrichment
            if (wptelefon != null) {
                format = wptelefon.substring(0, 2) + " " + wptelefon.substring(2, 4) + " " + wptelefon.substring(4, 6) + " " + wptelefon.substring(6, 8);
                item.addField("wptelefon", format);
            }

            //formatting yp numbers
            format = null;
            if (ypanynumber != null) {
                if (ypanynumber.length() == 8) {
                    //numbers starting with 8 should be formated as xxx xxxxx
                    if (ypanynumber.substring(0, 1).equals("8"))
                        format = ypanynumber.substring(0, 3) + " " + ypanynumber.substring(3, 8);
                    else
                        format = ypanynumber.substring(0, 2) + " " + ypanynumber.substring(2, 4) + " " + ypanynumber.substring(4, 6) + " " + ypanynumber.substring(6, 8);
                } else
                    format = ypanynumber;

                item.addField("ypanynumber", format);
            }

            //formatting yptelefax in yip
            if (yptelefax != null) {
                if (yptelefax.length() == 8)
                    format = yptelefax.substring(0, 2) + " " + yptelefax.substring(2, 4) + " " + yptelefax.substring(4, 6) + " " + yptelefax.substring(6, 8);
                else
                    format = yptelefax;

                item.addField("yptelefax", format);
            }


        }
    }
}