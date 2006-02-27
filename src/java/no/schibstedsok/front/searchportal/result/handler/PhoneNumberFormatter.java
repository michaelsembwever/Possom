// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * @author <a href="mailto:thomas.kjerstad@aftenposten.no">Thomas Kj�rstad</a>
 * @version <tt>$Revision$</tt>
 */
public class PhoneNumberFormatter implements ResultHandler {

    public void handleResult(final Context cxt, final Map parameters) {

        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            final String ypanynumber = searchResultItem.getField("ypanynumber");
            final String wpmobiltelefon = searchResultItem.getField("wpmobiltelefon");
            final String wptelefon = searchResultItem.getField("wptelefon");
            final String yptelefax = searchResultItem.getField("yptelefax");
            String format = null;

            // TODO: put formatting oh white numbers in here too. For now this will be formatted in MultiValueFieldCollector
            //formatting wp mobile numbers in enrichment
            if (wpmobiltelefon != null) {
                format = wpmobiltelefon.substring(0, 2) + " " + wpmobiltelefon.substring(2, 4) + " " + wpmobiltelefon.substring(4, 6) + " " + wpmobiltelefon.substring(6, 8);
                searchResultItem.addField("wpmobiltelefon", format);
            }

            //formatting wptelefon in enrichment
            if (wptelefon != null) {
                format = wptelefon.substring(0, 2) + " " + wptelefon.substring(2, 4) + " " + wptelefon.substring(4, 6) + " " + wptelefon.substring(6, 8);
                searchResultItem.addField("wptelefon", format);
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

                searchResultItem.addField("ypanynumber", format);
            }

            //formatting yptelefax in yip
            if (yptelefax != null) {
                if (yptelefax.length() == 8)
                    format = yptelefax.substring(0, 2) + " " + yptelefax.substring(2, 4) + " " + yptelefax.substring(4, 6) + " " + yptelefax.substring(6, 8);
                else
                    format = yptelefax;

                searchResultItem.addField("yptelefax", format);
            }


        }
    }
}