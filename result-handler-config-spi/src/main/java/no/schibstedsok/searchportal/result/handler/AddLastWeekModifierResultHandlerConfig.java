// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.result.handler.AbstractResultHandlerConfig.Controller;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Adds a modifier to the searchResult with a count for the last week based on day modifiers.
 * <p/>
 * <b>Note:</b> The implementation of this depends on that the day modifiers are sorted descending (last date first)
 * <b>Note:</b> This will only work on searchResults that are actually FastSearchResult
 * 
 * @version $Id$
 */
@Controller("AddLastWeekModifierResultHandler")
public final class AddLastWeekModifierResultHandlerConfig extends AbstractResultHandlerConfig {
    
    private static final Logger LOG = Logger.getLogger(AddLastWeekModifierResultHandlerConfig.class);
    
    private String dayFormat = "yyyy-MM-dd";
    private String timeZone = "UTC";
    private String dayModifierKey;
    private String targetNavigatorField;

    /**
     * 
     * @return 
     */
    public String getDayFormat() {
        return dayFormat;
    }

    /**
     * 
     * @param dayFormat 
     */
    public void setDayFormat(String dayFormat) {
        this.dayFormat = dayFormat;
    }

    /**
     * 
     * @return 
     */
    public String getDayModifierKey() {
        return dayModifierKey;
    }

    /**
     * 
     * @param dayModifierKey 
     */
    public void setDayModifierKey(String dayModifierKey) {
        this.dayModifierKey = dayModifierKey;
    }

    /**
     * 
     * @return 
     */
    public String getTargetNavigatorField() {
        return targetNavigatorField;
    }

    /**
     * 
     * @param targetNavigatorField 
     */
    public void setTargetNavigatorField(String targetNavigatorField) {
        this.targetNavigatorField = targetNavigatorField;
    }

    /**
     * 
     * @return 
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * 
     * @param timeZone 
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public AbstractResultHandlerConfig readResultHandler(final Element element) {
        
        super.readResultHandler(element);

        setDayModifierKey(element.getAttribute("day-modifier-key"));
        setTargetNavigatorField(element.getAttribute("target-navigator-field"));
        String optionalAttribute = element.getAttribute("day-format");
        if (optionalAttribute != null && optionalAttribute.length() > 0) {
            setDayFormat(optionalAttribute);
        }
        optionalAttribute = element.getAttribute("time-zone");
        if (optionalAttribute != null && optionalAttribute.length() > 0) {
            setTimeZone(optionalAttribute);
        }

        return this;
    }
    
    
}
