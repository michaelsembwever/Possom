// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Adds a modifier to the searchResult with a navigator to the last week based on day modifiers.
 * <p/>
 * <b>Note:</b> The implementation of this depends on that the day modifiers are sorted descending (last date first)
 * <b>Note:</b> This will only work on searchResults that are actually FastSearchResult
 */
public class AddLastWeekModifierResultHandler implements ResultHandler {
    private static final int DAYS_IN_WEEK = 7;
    private static final Logger LOG = Logger.getLogger(AddLastWeekModifierResultHandler.class);
    private String dayFormat = "yyyy-MM-dd";
    private String timeZone = "UTC";
    private String dayModifierKey;
    private String targetNavigatorField;

    public void handleResult(Context cxt, DataModel datamodel) {
        try {
            final SearchResult searchResult = cxt.getSearchResult();
            if (searchResult instanceof FastSearchResult) {
                int weekCount = 0;
                final FastSearchResult fastResult = (FastSearchResult) searchResult;
                final List<Modifier> dayModifiers = fastResult.getModifiers(dayModifierKey);
                final SimpleDateFormat sdf = new SimpleDateFormat(dayFormat);
                final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
                calendar.add(Calendar.DAY_OF_YEAR, -(DAYS_IN_WEEK + 1));

                // Going through day modifiers and counting
                int maxModifiersToProcess = Math.min(DAYS_IN_WEEK, dayModifiers.size());
                for (int i = 0; i < maxModifiersToProcess; i++) {
                    final Modifier modifier = dayModifiers.get(i);
                    final Date modifierDate = sdf.parse(modifier.getName());
                    if (modifierDate.after(calendar.getTime())) {
                        weekCount += modifier.getCount();
                    } else {
                        // Some dates missing, no point in going furthe into the list.
                        break;
                    }
                }
                // Creating the new modifier
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                String fromDate = sdf.format(calendar.getTime());
                LOG.debug("Creating modifier. Adding at " + targetNavigatorField + ", vaule=" + fromDate);
                Modifier newModifier = new Modifier(fromDate, weekCount, new Navigator(fromDate, targetNavigatorField, null, null));
                fastResult.addModifier(targetNavigatorField, newModifier);
            } else {
                LOG.error("Can not use " + AddLastWeekModifierResultHandler.class.getName() + " on a generic searchResult. Must be a " + FastSearchResult.class.getName());
            }
        } catch (ParseException e) {
            LOG.error("Could not parse navigator. Expected format is '" + dayFormat + "'", e);
        }
    }

    public String getDayFormat() {
        return dayFormat;
    }

    public void setDayFormat(String dayFormat) {
        this.dayFormat = dayFormat;
    }

    public String getDayModifierKey() {
        return dayModifierKey;
    }

    public void setDayModifierKey(String dayModifierKey) {
        this.dayModifierKey = dayModifierKey;
    }

    public String getTargetNavigatorField() {
        return targetNavigatorField;
    }

    public void setTargetNavigatorField(String targetNavigatorField) {
        this.targetNavigatorField = targetNavigatorField;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
