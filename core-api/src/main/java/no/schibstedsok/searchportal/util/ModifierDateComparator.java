package no.schibstedsok.searchportal.util;

import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.mode.command.AbstractSimpleFastSearchCommand;

import java.util.Comparator;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

/**
 * Enum for various reverse date comparators of navigator modifiers.
 *
 */
public enum ModifierDateComparator implements Comparator<Modifier> {

    YEAR("Y"),
    MONTH_YEAR("M-Y"),
    DAY_MONTH_YEAR("d-M-Y");

    private static final Logger LOG = Logger.getLogger(AbstractSimpleFastSearchCommand.class);
    private static final String ERR_PARSE_ERROR = "Unable to parse date {0} or {1}";

    private final String format;

    /**
     *
     * @param format A DateFormat string.
     */
    ModifierDateComparator(final String format) {
        this.format = format;
    }

    /** @{inheritDoc} */
    public int compare(final Modifier m1, final Modifier m2) {

        final DateFormat fmt = new SimpleDateFormat(format);

        try {

            final Date d1 = fmt.parse(m1.getName());
            final Date d2 = fmt.parse(m2.getName());

            return d2.compareTo(d1);

        } catch (ParseException e) {
            LOG.warn(MessageFormat.format(ERR_PARSE_ERROR, m1.getName(), m2.getName()));
            return 0;
        }
    }
}
