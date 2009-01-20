/*   Copyright (2008) Schibsted Søk AS
 *   This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * AgeFilterTransformer.java
 *
 */

package no.sesat.search.query.transform;


import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @version $Id$
 */
public final class AgefilterQueryTransformer extends AbstractQueryTransformer {
    private static final Logger LOG = Logger.getLogger(AgefilterQueryTransformer.class);

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String AGE_PARAMETER = "age";

    private final AgefilterQueryTransformerConfig config;

    /**
     * @param config
     */
    public AgefilterQueryTransformer(final QueryTransformerConfig config) {
        this.config = (AgefilterQueryTransformerConfig) config;
    }

    @Override
    public String getFilter(final Map parameters) {

        String ageSymbol = config.getAgeSymbol();
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

            return "+" + config.getAgeField() + ":>" + df.format(cal.getTime());
        } else {
            return "";
        }
    }

    /**
     * @param ageSymbol
     * @return Number of seconds that one unit of the ageSymbol represents.
     */
    public int getAgeInSeconds(final String ageSymbol) {
        if (ageSymbol.equals("w")) {
            return 60 * 60 * 24 * 7;
        } else if (ageSymbol.equals("m")) {
            return 60 * 60 * 24 * 30;
        } else if (ageSymbol.equals("d")) {
            return 60 * 60 * 24;
        } else if (ageSymbol.equals("h")) {
            return 60 * 60;
        } else {
            throw new IllegalArgumentException("Unknown age symbol: " + ageSymbol);
        }
    }

}
