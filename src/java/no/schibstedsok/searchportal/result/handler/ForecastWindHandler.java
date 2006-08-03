package no.schibstedsok.searchportal.result.handler;

import java.util.Map;

import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * A Storm result handler that looks into nested searchresults for the field to
 * modify. Needed because we get raw data from Storm.
 * 
 * @author larsj
 * 
 */
public class ForecastWindHandler implements ResultHandler {

	public void handleResult(Context cxt, Map parameters) {

		for (final SearchResultItem item : cxt.getSearchResult().getResults()) {

			//see if there are any forecasts for the location.
			if (item.getNestedSearchResult("forecasts") != null) {
				for (final SearchResultItem forecast : item
						.getNestedSearchResult("forecasts").getResults()) {

					final int direction = Integer.parseInt(forecast
							.getField("winddirection"));
					final int condition = Integer.parseInt(forecast
							.getField("symbol"));
					final float speed = Float.parseFloat(forecast
							.getField("windspeed"));

					if (0 <= direction && direction <= 22) {
						forecast.addField("winddirectionText", "N");
					} else if (338 <= direction && direction <= 359) {
						forecast.addField("winddirectionText", "N");
					} else if (293 <= direction && direction <= 337) {
						forecast.addField("winddirectionText", "NW");
					} else if (248 <= direction && direction <= 292) {
						forecast.addField("winddirectionText", "W");
					} else if (203 <= direction && direction <= 247) {
						forecast.addField("winddirectionText", "SW");
					} else if (158 <= direction && direction <= 202) {
						forecast.addField("winddirectionText", "S");
					} else if (113 <= direction && direction <= 157) {
						forecast.addField("winddirectionText", "SE");
					} else if (68 <= direction && direction <= 112) {
						forecast.addField("winddirectionText", "E");
					} else if (23 <= direction && direction <= 67) {
						forecast.addField("winddirectionText", "NE");
					} else {
						forecast.addField("winddirectionText", "UNKNOWN");
					}

					if (0 <= speed && speed <= 0.3) {
						forecast.addField("windspeedText", "Vindstille");
					} else if (0.3 < speed && speed <= 1.6) {
						forecast.addField("windspeedText", "Flauvind");
					} else if (1.6 < speed && speed <= 3.4) {
						forecast.addField("windspeedText", "Svakvind");
					} else if (3.4 < speed && speed <= 5.5) {
						forecast.addField("windspeedText", "Lettbris");
					} else if (5.5 < speed && speed <= 8.0) {
						forecast.addField("windspeedText", "Laberbris");
					} else if (8.0 < speed && speed <= 10.8) {
						forecast.addField("windspeedText", "Friskbris");
					} else if (10.8 < speed && speed <= 13.9) {
						forecast.addField("windspeedText", "Litenkuling");
					} else if (13.9 < speed && speed <= 17.2) {
						forecast.addField("windspeedText", "Stivkuling");
					} else if (17.2 < speed && speed <= 20.8) {
						forecast.addField("windspeedText", "Sterkkuling");
					} else if (20.8 < speed && speed <= 24.5) {
						forecast.addField("windspeedText", "Litenstorm");
					} else if (24.5 < speed && speed <= 28.5) {
						forecast.addField("windspeedText", "Fullstorm");
					} else if (28.5 < speed && speed <= 32.6) {
						forecast.addField("windspeedText", "Sterkstorm");
					} else if (32.6 < speed) {
						forecast.addField("windspeedText", "Orkan");
					}

					forecast.addField("conditionText", "condition" + condition);

				}
			}

		}

	}

}
