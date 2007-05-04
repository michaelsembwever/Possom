// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.util;

import no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand;
import no.schibstedsok.searchportal.result.Modifier;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Enum for various reverse date comparators of navigator modifiers.
 */
public enum ModifierDateComparator implements Comparator<Modifier> {

    YEAR("y", SortOrder.DESCENDING),
    MONTH_YEAR("M-y", SortOrder.ASCENDING),
    DAY_MONTH_YEAR("d-M-y", SortOrder.ASCENDING),
    DAY_MONTH_YEAR_DESCENDING("d-M-y", SortOrder.DESCENDING),
    YEAR_MONTH("y-M", SortOrder.DESCENDING);

    private static final Logger LOG = Logger.getLogger(AbstractSimpleFastSearchCommand.class);
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
