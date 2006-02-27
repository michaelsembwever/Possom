// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.Map;
import java.util.Iterator;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * WeatherDateHandler is part of no.schibstedsok.front.searchportal.result
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
    private String sourceField;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    public void handleResult(final Context cxt, final Map parameters) {

        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();
            final String datestring = searchResultItem.getField(sourceField);
            Date date = null;

            try {
                date = sdf.parse(datestring);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            searchResultItem.addField("datePart", datePart.format(date));
            searchResultItem.addField("timePart", timePart.format(date));


        }
    }
}
