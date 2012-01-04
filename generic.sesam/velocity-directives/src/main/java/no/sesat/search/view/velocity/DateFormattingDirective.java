/* Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * DateFormattingDirective.java
 *
 * Created on 24. november 2006, 11:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.sesat.search.view.velocity;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * A velocity directive to format newsnavigator date.
 * <p/>
 * Newsdate comes from the fastnavigator in four forms:<br/>
 * 1. 10-2006     -> Oktober 2006<br/>
 * 2. 24-10-2006  -> 24. oktober 2006<br/>
 * 3. 2006-10     -> Oktober 2006<br/>
 * 4. 2006-10-24  -> 24. oktober 2006<br/>
 * <p/>
 * if 'newsdateOnly' navigator, we shuold check if the date is today or yesterday
 * <p/>
 * <b>Note:</b> This directive is meant to be used on navigators. that means that the timeZone for the dates are in "CET"
 */
public final class DateFormattingDirective extends Directive {
    // We could get Locale from the site in the context, but then we had to create a new
    // dateformatter for each call. (can be up to 100 calls for one request.) Leaving it to "no" only for now.
    private static DateFormatSymbols formatSymbols = new DateFormatSymbols(new Locale("no"));

//    static {
//        // Default symbols are all lowercase for "no", so we have to set new ones.
//        formatSymbols.setMonths(new String[]{"Januar", "Februar", "Mars", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Desember"});
//    }

    private static SimpleDateFormat shortFormatter = new SimpleDateFormat("MMMMM yyyy", formatSymbols);
    private static SimpleDateFormat longFormatter = new SimpleDateFormat("d. MMMMM yyyy", formatSymbols);

    private static final Logger LOG = Logger.getLogger(DateFormattingDirective.class);

    private static final String NAME = "dateFormatting";

    private static SimpleDateFormat formOneParser = new SimpleDateFormat("MM-yyyy");
    private static SimpleDateFormat formTwoParser = new SimpleDateFormat("dd-MM-yyyy");
    private static SimpleDateFormat formThreeParser = new SimpleDateFormat("yyyy-MM");
    private static SimpleDateFormat formFourParser = new SimpleDateFormat("yyyy-MM-dd");


    /**
     * {@inheritDoc}
     */
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    public int getType() {
        return LINE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if (node.jjtGetNumChildren() < 1) {
            rsvc.error("#" + getName() + " - missing argument");
            return false;
        }

        final String input = node.jjtGetChild(0).value(context).toString();

        String navName = null;
        if (node.jjtGetNumChildren() > 1) {
            navName = node.jjtGetChild(1).value(context).toString();
        }

        String fDate = input;
        try {
            fDate = formatDate(input, navName);
        } catch (ParseException e) {
            LOG.error("Could not parse date", e);
        }
        writer.write(fDate);

        final Token lastToken = node.getLastToken();

        if (lastToken.image.endsWith("\n")) {
            writer.write("\n");
        }

        return true;
    }

    protected String formatDate(String input, String navName) throws ParseException {
        if (input.substring(2, 3).equals("-")) {
            // Form one or two
            if (input.length() == 10) {
                return formatFormTwo(input, "newsdateOnly".equals(navName));
            } else {
                return StringUtils.capitalize(formatFormOne(input));
            }
        } else {
            // From three or four
            if (input.length() == 10) {
                return formatFormFour(input, "newsdateOnly".equals(navName));
            } else {
                return StringUtils.capitalize(formatFormThree(input));
            }
        }
    }

    protected String formatFormOne(String input) throws ParseException {
        // 1. 10-2006     -> oktober 2006
        return shortFormatter.format(formOneParser.parse(input));
    }

    protected String formatFormTwo(String input, boolean newsDateOnly) throws ParseException {
        // 2. 24-10-2006  -> 24. oktober 2006
        Date parsedDate = formTwoParser.parse(input);
        return longFormat(parsedDate, newsDateOnly);
    }

    protected String formatFormThree(String input) throws ParseException {
        // 3. 2006-10     -> oktober 2006
        return shortFormatter.format(formThreeParser.parse(input));
    }

    protected String formatFormFour(String input, boolean newsDateOnly) throws ParseException {
        Date parsedDate = formFourParser.parse(input);
        return longFormat(parsedDate, newsDateOnly);
    }

    private String longFormat(Date parsedDate, boolean newsDateOnly) {
        if (newsDateOnly) {
            if (isToday(parsedDate)) {
                return "I dag";
            } else if (isYesterday(parsedDate)) {
                return "I g&#229;r";
            }
        }
        return longFormatter.format(parsedDate);
    }

    private boolean isToday(Date parsedDate) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CET"));
        int todayDay = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        cal.setTime(parsedDate);
        return todayDay == cal.get(Calendar.DAY_OF_YEAR) && year == cal.get(Calendar.YEAR);
    }

    private boolean isYesterday(Date parsedDate) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CET"));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        int yesterdayDay = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        cal.setTime(parsedDate);
        return yesterdayDay == cal.get(Calendar.DAY_OF_YEAR) && year == cal.get(Calendar.YEAR);
    }

}
