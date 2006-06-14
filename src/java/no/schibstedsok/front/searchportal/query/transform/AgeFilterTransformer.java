/*
 * AgeFilterTransformer.java
 *
 */

package no.schibstedsok.front.searchportal.query.transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @author maek
 */
public class AgeFilterTransformer extends AbstractQueryTransformer {

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final static String AGE_PARAMETER = "age";
    
    private String ageField; // In seconds
    
    public AgeFilterTransformer() {
    }

    public String getFilter(final Map parameters) {
        final String ageSymbol = parameters.get(AGE_PARAMETER) instanceof String[]
                ? ((String[])parameters.get(AGE_PARAMETER))[0]
                : (String)parameters.get(AGE_PARAMETER);
        
        if (ageSymbol != null && !ageSymbol.equals("")) {
            final Calendar cal = Calendar.getInstance();

            cal.add(Calendar.SECOND, -getAgeInSeconds(ageSymbol));

            final DateFormat df = new SimpleDateFormat(FAST_DATE_FMT);

            // Zulu time is UTC. But java doesn't know that.
            if (FAST_DATE_FMT.endsWith("'Z'")) {
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
            }

            return "+" + ageField + ":>" + df.format(cal.getTime());
        } else {
            return "";
        }
    }

    public void setAgeField(final String ageField) {
        this.ageField = ageField;
    }

    public Object clone() throws CloneNotSupportedException {
        final AgeFilterTransformer retValue = (AgeFilterTransformer)super.clone();
        retValue.ageField = ageField;
        return retValue;
    }

    private int getAgeInSeconds(String ageSymbol) {
        if (ageSymbol.equals("w")) {
            return 60 * 60 * 24 * 7;
        } else if (ageSymbol.equals("m")) {
            return 60 * 60 * 24 * 30;
        } else if (ageSymbol.equals("h")) {
            return 60 * 60;
        } else if (ageSymbol.equals("d")) {
            return 60 * 60 * 24;
        } else {
            throw new IllegalArgumentException("Unknown age symbol: " + ageSymbol);
        }
    }
}
