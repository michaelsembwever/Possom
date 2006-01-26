package no.schibstedsok.front.searchportal.result;


import java.util.Map;
import java.util.Iterator;
import java.text.ParseException;
import java.text.DecimalFormat;

/**
 * WeatherCelciusHandler is part of no.schibstedsok.front.searchportal.result
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class WeatherCelciusHandler implements ResultHandler  {

    private String targetField;
    private String sourceField;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public void handleResult(Context cxt, Map parameters) {


        for (Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();
            String celcius = searchResultItem.getField(sourceField);
            String newVal = null;

            try {
                newVal = new DecimalFormat("#").parse(celcius) + "";
            } catch (ParseException e) {
                newVal = celcius;
            }
            if("-0".equals(newVal)){ newVal = "0"; }
            searchResultItem.addField(targetField, newVal);
        }
    }
}
