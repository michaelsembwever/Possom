// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * DateFormatHandler is part of no.schibstedsok.front.searchportal.result
 * Transform fast inputdate to how it will be displayed in tv enrichment . Tv Enrichment
 * will most likely only display Hour of day.
 * 
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class DateFormatHandler implements ResultHandler {

    public enum Fields {
        YEAR,
        MONTH,
        DAY,
        HOUR,
        MINUTE,
        SECOND;
    }
    
    private String fieldPrefix = "";
    private String sourceField;

    public String getFieldPrefix() {
        return fieldPrefix;
    }

    public void setFieldPrefix(final String fieldPrefix) {
        this.fieldPrefix = fieldPrefix;
    }

    public void setSourceField(final String sourceField) {
       this.sourceField = sourceField;
    }

    public String getSourceField() {
        return sourceField;
    }
    
    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {

            final String docDateTime = item.getField(sourceField);

            if (docDateTime != null) {
                String year = docDateTime.substring(0,4);
                item.addField(fieldPrefix + Fields.YEAR.name(), year);

                String month = docDateTime.substring(5,7);
                item.addField(fieldPrefix + Fields.MONTH.name(), month);

                String day = docDateTime.substring(8,10);
                item.addField(fieldPrefix + Fields.DAY.name(), day);

                String hour = docDateTime.substring(11, 13);
                item.addField(fieldPrefix + Fields.HOUR.name(), hour);

                String minute = docDateTime.substring(14, 16);
                item.addField(fieldPrefix + Fields.MINUTE.name(), minute);

                String second = docDateTime.substring(17, 19);
                item.addField(fieldPrefix + Fields.SECOND.name(), second);
            }

        }
    }
}
