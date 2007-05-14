// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.text.ParseException;
import java.text.DecimalFormat;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;


/**
 * WeatherCelciusHandler is part of no.schibstedsok.searchportal.result
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
public final class WeatherCelciusHandler implements ResultHandler  {

    private final WeatherCelciusResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public WeatherCelciusHandler(final ResultHandlerConfig config){
        this.config = (WeatherCelciusResultHandlerConfig)config;
    }
    
    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {
        
        for (final ResultItem item : cxt.getSearchResult().getResults()) {
            
            final String celcius = item.getField(config.getSourceField());
            String newVal = null;

            try {
                newVal = new DecimalFormat("#").parse(celcius) + "";
                
            } catch (ParseException e) {
                newVal = celcius;
            }
            
            if ("-0".equals(newVal)) { newVal = "0"; }
            
            cxt.getSearchResult().replaceResult(item, item.addField(config.getTargetField(), newVal));
        }
    }
}
