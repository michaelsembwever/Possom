package no.schibstedsok.front.searchportal.result;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.Map;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DecimalFormat;
import java.math.BigDecimal;

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

    public void handleResult(SearchResult result, Map parameters) {


        for (Iterator iterator = result.getResults().iterator(); iterator.hasNext();) {
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
