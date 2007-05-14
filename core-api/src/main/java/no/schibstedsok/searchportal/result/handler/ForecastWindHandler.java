// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * A Storm result handler that looks into nested searchresults for the field to
 * modify. Needed because we get raw data from Storm.
 *
 * @author larsj
 * @version $Id$
 */
public final class ForecastWindHandler implements ResultHandler {
    
    private final ForecastWindResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public ForecastWindHandler(final ResultHandlerConfig config){
        this.config = (ForecastWindResultHandlerConfig)config;
    }

    /** {@inherit} **/
	public void handleResult(final Context cxt, final DataModel datamodel) {

		for (final ResultItem item : cxt.getSearchResult().getResults()) {

			//see if there are any forecasts for the location.
			if (item instanceof ResultList<?>) {
                
                final ResultList<ResultItem> forecasts = (ResultList<ResultItem>)item;
				for (final ResultItem f : forecasts.getResults()) {

					final int direction = Integer.parseInt(f.getField("winddirection"));
					final int condition = Integer.parseInt(f.getField("symbol"));
					final float speed = Float.parseFloat(f.getField("windspeed"));
                    
                    ResultItem forecast = f;

					if (0 <= direction && direction <= 22) {
						forecast = forecast.addField("winddirectionText", "N");
					} else if (338 <= direction && direction <= 359) {
						forecast = forecast.addField("winddirectionText", "N");
					} else if (293 <= direction && direction <= 337) {
						forecast = forecast.addField("winddirectionText", "NW");
					} else if (248 <= direction && direction <= 292) {
						forecast = forecast.addField("winddirectionText", "W");
					} else if (203 <= direction && direction <= 247) {
						forecast = forecast.addField("winddirectionText", "SW");
					} else if (158 <= direction && direction <= 202) {
						forecast = forecast.addField("winddirectionText", "S");
					} else if (113 <= direction && direction <= 157) {
						forecast = forecast.addField("winddirectionText", "SE");
					} else if (68 <= direction && direction <= 112) {
						forecast = forecast.addField("winddirectionText", "E");
					} else if (23 <= direction && direction <= 67) {
						forecast = forecast.addField("winddirectionText", "NE");
					} else {
						forecast = forecast.addField("winddirectionText", "UNKNOWN");
					}

					if (0 <= speed && speed <= 0.3) {
						forecast = forecast.addField("windspeedText", "Vindstille");
					} else if (0.3 < speed && speed <= 1.6) {
						forecast = forecast.addField("windspeedText", "Flauvind");
					} else if (1.6 < speed && speed <= 3.4) {
						forecast = forecast.addField("windspeedText", "Svakvind");
					} else if (3.4 < speed && speed <= 5.5) {
						forecast = forecast.addField("windspeedText", "Lettbris");
					} else if (5.5 < speed && speed <= 8.0) {
						forecast = forecast.addField("windspeedText", "Laberbris");
					} else if (8.0 < speed && speed <= 10.8) {
						forecast = forecast.addField("windspeedText", "Friskbris");
					} else if (10.8 < speed && speed <= 13.9) {
						forecast = forecast.addField("windspeedText", "Litenkuling");
					} else if (13.9 < speed && speed <= 17.2) {
						forecast = forecast.addField("windspeedText", "Stivkuling");
					} else if (17.2 < speed && speed <= 20.8) {
						forecast = forecast.addField("windspeedText", "Sterkkuling");
					} else if (20.8 < speed && speed <= 24.5) {
						forecast = forecast.addField("windspeedText", "Litenstorm");
					} else if (24.5 < speed && speed <= 28.5) {
						forecast = forecast.addField("windspeedText", "Fullstorm");
					} else if (28.5 < speed && speed <= 32.6) {
						forecast = forecast.addField("windspeedText", "Sterkstorm");
					} else if (32.6 < speed) {
						forecast = forecast.addField("windspeedText", "Orkan");
					}

					forecast = forecast.addField("conditionText", "condition" + condition);

                    forecasts.replaceResult(f, forecast);
				}
			}

		}

	}

}
