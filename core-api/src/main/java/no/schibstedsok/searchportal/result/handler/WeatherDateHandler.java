// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * WeatherDateHandler is part of no.schibstedsok.searchportal.result
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
public class WeatherDateHandler implements ResultHandler  {

    /**
     * 
     */
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    /**
     * 
     */
    protected static final SimpleDateFormat timePart = new SimpleDateFormat("HH:mm");
    /**
     * 
     */
    protected static final SimpleDateFormat datePart = new SimpleDateFormat("dd.MM.yyyy");
    
    private final WeatherDateResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public WeatherDateHandler(final ResultHandlerConfig config){
        this.config = (WeatherDateResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String datestring = item.getField(config.getSourceField());
            Date date = null;

            try {
                date = sdf.parse(datestring);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            item.addField("datePart", datePart.format(date));
            item.addField("timePart", timePart.format(date));
        }
    }
}
