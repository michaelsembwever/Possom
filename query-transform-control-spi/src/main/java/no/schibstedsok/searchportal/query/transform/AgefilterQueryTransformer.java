/*
 * AgeFilterTransformer.java
 *
 */

package no.schibstedsok.searchportal.query.transform;

import org.w3c.dom.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author maek
 */
public final class AgefilterQueryTransformer extends AbstractQueryTransformer {

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String AGE_PARAMETER = "age";

    private String ageField; // In seconds
    private String ageSymbol;

    @Override
    public String getFilter(final Map parameters) {
        if (ageSymbol == null) {
            ageSymbol = parameters.get(AGE_PARAMETER) instanceof String[]
                    ? ((String[]) parameters.get(AGE_PARAMETER))[0]
                    : (String) parameters.get(AGE_PARAMETER);
        }

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

    public void setAgeSymbol(final String ageSymbol) {
        this.ageSymbol = ageSymbol;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final AgefilterQueryTransformer retValue = (AgefilterQueryTransformer) super.clone();
        retValue.ageField = ageField;
        retValue.ageSymbol = ageSymbol;
        return retValue;
    }

    private int getAgeInSeconds(final String ageSymbol) {
        if (ageSymbol.equals("w")) {
            return 60 * 60 * 24 * 7;
        } else if (ageSymbol.equals("m")) {
            return 60 * 60 * 24 * 30;
        } else if (ageSymbol.equals("h")) {
            return 60 * 60;
        } else if (ageSymbol.equals("d")) {
            return 60 * 60 * 24;
        } else if (ageSymbol.equals("h")) {
            return 60 * 60;
        } else {
            throw new IllegalArgumentException("Unknown age symbol: " + ageSymbol);
        }
    }

    @Override
    public QueryTransformer readQueryTransformer(final Element qt) {

        super.readQueryTransformer(qt);
        setAgeField(qt.getAttribute("field"));
        String optionalAttribute = qt.getAttribute("age-symbol");
        if (optionalAttribute != null && optionalAttribute.length() > 0) {
            setAgeSymbol(optionalAttribute);
        }
        return this;
    }
}
