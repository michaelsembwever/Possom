// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;

/**
 * @author <a href="mailto:thomas.kjerstad@aftenposten.no">Thomas Kjærstad</a>
 * @version <tt>$Id$</tt>
 */
public final class PhoneNumberFormatter implements ResultHandler {
    
    private final PhoneNumberFormatterResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public PhoneNumberFormatter(final ResultHandlerConfig config){
        this.config = (PhoneNumberFormatterResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final ResultItem i : cxt.getSearchResult().getResults()) {
            
            final String ypanynumber = i.getField("ypanynumber");
            final String wpmobiltelefon = i.getField("wpmobiltelefon");
            final String wptelefon = i.getField("wptelefon");
            final String yptelefax = i.getField("yptelefax");
            String format = null;
            
            ResultItem item = i;

            // TODO: put formatting oh white numbers in here too. For now this will be formatted in MultiValueFieldCollector
            //formatting wp mobile numbers in enrichment
            if (wpmobiltelefon != null) {
                format = wpmobiltelefon.substring(0, 2) + " " + wpmobiltelefon.substring(2, 4) + " " + wpmobiltelefon.substring(4, 6) + " " + wpmobiltelefon.substring(6, 8);
                item = item.addField("wpmobiltelefon", format);
            }

            //formatting wptelefon in enrichment
            if (wptelefon != null) {
                format = wptelefon.substring(0, 2) + " " + wptelefon.substring(2, 4) + " " + wptelefon.substring(4, 6) + " " + wptelefon.substring(6, 8);
                item = item.addField("wptelefon", format);
            }

            //formatting yp numbers
            format = null;
            if (ypanynumber != null) {
                if (ypanynumber.length() == 8) {
                    //numbers starting with 8 should be formated as xxx xxxxx
                    if (ypanynumber.substring(0, 1).equals("8")){
                        format = ypanynumber.substring(0, 3) + " " + ypanynumber.substring(3, 8);
                    }else{
                        format = ypanynumber.substring(0, 2) + " " + ypanynumber.substring(2, 4) + " " + ypanynumber.substring(4, 6) + " " + ypanynumber.substring(6, 8);
                    }
                } else{
                    format = ypanynumber;
                }
                item = item.addField("ypanynumber", format);
            }

            //formatting yptelefax in yip
            if (yptelefax != null) {
                if (yptelefax.length() == 8){
                    format = yptelefax.substring(0, 2) + " " + yptelefax.substring(2, 4) + " " + yptelefax.substring(4, 6) + " " + yptelefax.substring(6, 8);
                }else{
                    format = yptelefax;
                }
                item = item.addField("yptelefax", format);
            }

            cxt.getSearchResult().replaceResult(i, item);
        }
    }
}