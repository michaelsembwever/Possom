/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.result;

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Enum for various reverse date comparators of navigator modifiers.
 * @version $Id$
 */
public enum ModifierDateComparator implements Comparator<Modifier> {

    YEAR("y", SortOrder.DESCENDING),
    MONTH_YEAR("M-y", SortOrder.ASCENDING),
    DAY_MONTH_YEAR("d-M-y", SortOrder.ASCENDING),
    DAY_MONTH_YEAR_DESCENDING("d-M-y", SortOrder.DESCENDING),
    YEAR_MONTH_DAY_DESCENDING("y-M-d", SortOrder.DESCENDING),
    YEAR_MONTH("y-M", SortOrder.DESCENDING);

    private static final Logger LOG = Logger.getLogger(ModifierDateComparator.class);
    private static final String ERR_PARSE_ERROR = "Unable to parse date {0} or {1}";

    private final String format;
    private final SortOrder sortOrder;

    private enum SortOrder {
        ASCENDING,
        DESCENDING,
    }

    /**
     * @param format A DateFormat string.
     */
    ModifierDateComparator(final String format, final SortOrder sortOrder) {
        this.format = format;
        this.sortOrder = sortOrder;
    }

    /**
     * @{inheritDoc}
     */
    public int compare(final Modifier m1, final Modifier m2) {

        final DateFormat fmt = new SimpleDateFormat(format);

        try {

            final Date d1 = fmt.parse(m1.getName());
            final Date d2 = fmt.parse(m2.getName());

            switch (sortOrder) {
                case ASCENDING:
                    return d1.compareTo(d2);
                case DESCENDING:
                    return d2.compareTo(d1);
                default:
                    return d2.compareTo(d1);
            }
        } catch (ParseException e) {
            LOG.warn(MessageFormat.format(ERR_PARSE_ERROR, m1.getName(), m2.getName()));
            return 0;
        }
    }
}
