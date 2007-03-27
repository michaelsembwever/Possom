/*
 * AgeFilterTransformer.java
 *
 */

package no.schibstedsok.searchportal.query.transform;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author maek
 * @version $Id$
 */
public final class AgefilterQueryTransformer extends AbstractQueryTransformer {

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String AGE_PARAMETER = "age";

    private final AgefilterQueryTransformerConfig config;

    /**
     *
     * @param config
     */
    public AgefilterQueryTransformer(final QueryTransformerConfig config){
        this.config = (AgefilterQueryTransformerConfig) config;
    }

    @Override
    public String getFilter(final Map parameters) {

        String ageSymbol = config.getAgeSymbol();
        if (config.getAgeSymbol() == null) {
            ageSymbol = parameters.get(AGE_PARAMETER) instanceof String[]
                    ? ((String[]) parameters.get(AGE_PARAMETER))[0]
                    : (String) parameters.get(AGE_PARAMETER);
        }

        if (config.getAgeSymbol() != null && !config.getAgeSymbol().equals("")) {
            final Calendar cal = Calendar.getInstance();

            cal.add(Calendar.SECOND, -getAgeInSeconds(ageSymbol));

            final DateFormat df = new SimpleDateFormat(FAST_DATE_FMT);

            // Zulu time is UTC. But java doesn't know that.
            if (FAST_DATE_FMT.endsWith("'Z'")) {
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
            }

            return "+" + ageSymbol + ":>" + df.format(cal.getTime());
        } else {
            return "";
        }
    }

    /**
     *
     * @param ageSymbol
     * @return
     */
    public int getAgeInSeconds(final String ageSymbol) {
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

}
