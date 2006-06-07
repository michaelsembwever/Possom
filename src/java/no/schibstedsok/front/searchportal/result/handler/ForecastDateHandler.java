package no.schibstedsok.front.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * A Storm result handler that looks into nested searchresults for 
 * the field to modify.
 * 
 * @author larsj
 *
 */
public class ForecastDateHandler extends WeatherDateHandler {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");

	@Override
	public void handleResult(Context cxt, Map parameters) {


        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
        
        	for(final SearchResultItem forecast : item.getNestedSearchResult("forecasts").getResults()) {
        		
            	final String datestring = forecast.getField(sourceField);
                
                if(datestring != null){
                	Date date = null;
                	Calendar cal = new GregorianCalendar(new Locale("no", "no"));
                    try {
                        date = sdf.parse(datestring);
                        cal.setTime(date);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }
                    forecast.addField("datePart", datePart.format(date));
                    forecast.addField("timePart", timePart.format(date));
                    switch (cal.get(Calendar.DAY_OF_WEEK)) {
					case 2:
	                    forecast.addField("weekday", "day0");
						break;
					case 3:
	                    forecast.addField("weekday", "day1");
						break;
					case 4:
	                    forecast.addField("weekday", "day2");
						break;
					case 5:
	                    forecast.addField("weekday", "day3");
						break;
					case 6:
	                    forecast.addField("weekday", "day4");
						break;
					case 7:
	                    forecast.addField("weekday", "day5");
						break;
					case 1:
	                    forecast.addField("weekday", "day6");
						break;
					default:
						break;
					}
                }
        	}

        }
	
	}
}
