// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Map;

import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * WeatherCelciusHandler is part of no.schibstedsok.searchportal.result
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version $Id$
 */
public class WeatherCelciusHandler2 implements ResultHandler  {

    private String targetField;
    private String sourceField;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String celcius = item.getField(sourceField);
            String newVal = null;

            try {
                newVal = new DecimalFormat("#").parse(celcius) + "";
            } catch (ParseException e) {
                newVal = celcius;
            }

            if ("-0".equals(newVal)) { newVal = "0"; }
            item.addField(targetField, newVal);
        }
    }
}
