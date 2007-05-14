// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * A Storm result handler that looks into nested searchresults for
 * the field to modify.
 *
 * @author larsj
 * @version $Id$
 */
public final class ForecastDateHandler extends WeatherDateHandler {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    
    private final ForecastDateResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public ForecastDateHandler(final ResultHandlerConfig config){
        super(config);
        this.config = (ForecastDateResultHandlerConfig)config;
    }

    @Override
    public void handleResult(final Context cxt, final DataModel datamodel) {


        for (final ResultItem item : cxt.getSearchResult().getResults()) {

            //see if there are any forecasts for the location.
            if(item instanceof ResultList<?>){

                final ResultList<ResultItem> forecasts = (ResultList<ResultItem>)item;
                for(final ResultItem f : forecasts.getResults()) {

                    final String datestring = f.getField(config.getSourceField());

                    if(datestring != null){
                        
                        Date date = null;
                        final Calendar cal = new GregorianCalendar(new Locale("no", "no"));
                        try {
                            date = sdf.parse(datestring);
                            cal.setTime(date);
                            
                        } catch (ParseException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                        
                        ResultItem forecast = f;
                        forecast = forecast.addField("datePart", datePart.format(date));
                        forecast = forecast.addField("timePart", timePart.format(date));

                        if (cal.get(Calendar.MONTH) < 9) {
                            forecast = forecast.addField("month", "0" + Integer.toString(cal.get(Calendar.MONTH) + 1));
                        } else {
                            forecast = forecast.addField("month", Integer.toString(cal.get(Calendar.MONTH) + 1));
                        }
                        
                        forecast = forecast.addField("day", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                        forecast = forecast.addField("year", Integer.toString(cal.get(Calendar.YEAR)));

                        switch (cal.get(Calendar.DAY_OF_WEEK)) {
                            case 2:
                                forecast = forecast.addField("weekday", "day0");
                                break;
                            case 3:
                                forecast = forecast.addField("weekday", "day1");
                                break;
                            case 4:
                                forecast = forecast.addField("weekday", "day2");
                                break;
                            case 5:
                                forecast = forecast.addField("weekday", "day3");
                                break;
                            case 6:
                                forecast = forecast.addField("weekday", "day4");
                                break;
                            case 7:
                                forecast = forecast.addField("weekday", "day5");
                                break;
                            case 1:
                                forecast = forecast.addField("weekday", "day6");
                                break;
                            default:
                                break;
                        }
                        
                        forecasts.replaceResult(f, forecast);
                    }
                }

            }

        }

    }
}
