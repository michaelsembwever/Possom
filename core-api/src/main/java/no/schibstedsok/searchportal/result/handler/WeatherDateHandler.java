// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * WeatherDateHandler is part of no.schibstedsok.searchportal.result
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class WeatherDateHandler implements ResultHandler  {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    static SimpleDateFormat timePart = new SimpleDateFormat("HH:mm");
    static SimpleDateFormat datePart = new SimpleDateFormat("dd.MM.yyyy");

    private String targetField;
    protected String sourceField;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String datestring = item.getField(sourceField);
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
