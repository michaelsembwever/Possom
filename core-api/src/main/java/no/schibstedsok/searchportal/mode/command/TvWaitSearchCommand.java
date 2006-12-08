/*
 * TvWaitSearchCommand.java
 *
 * Created on 26 October 2006, 14:03
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import no.schibstedsok.searchportal.mode.config.TvWaitSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 *
 * @author andersjj
 */
public class TvWaitSearchCommand extends AbstractSimpleFastSearchCommand {
    
    /** Logger */
    private static final Logger LOG = Logger.getLogger(TvWaitSearchCommand.class);
    
    /** Search command configuration */
    private final TvWaitSearchConfiguration config;
    
    /** Wait on search result */
    private FastSearchResult wosr;

    /** Index to use when creating filters */
    private final int index;
    
    /** Millis in day */
    private static final int MILLIS_IN_DAY = 86400000;

    /** Sort options */
    private static enum SortBy {
        CHANNEL(),
        DAY(),
        CATEGORY();
    }
    
    /** Sort mode */
    private final SortBy userSortBy; 
   
    /** Blank query */
    private final boolean blankQuery;
    
    /** Execute query */
    private boolean executeQuery = true;
    
    /** Creates a new instance of TvWaitSearchCommand */
    public TvWaitSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
        
        blankQuery = cxt.getQuery().isBlank();
        this.config = (TvWaitSearchConfiguration) cxt.getSearchConfiguration();
        
        final String defaultUserSortBy = blankQuery ? "CHANNEL" : "DAY";
        final String usbp = getParameters().containsKey("userSortBy") ? ((String) getParameters().get("userSortBy")).toUpperCase() : defaultUserSortBy;
        userSortBy = SortBy.valueOf(usbp);
        
        
        final Object v = getParameters().get("offset");
        final int offset = Integer.parseInt(v instanceof String[] && ((String[]) v).length == 1 
                ? ((String[]) v)[0]
                : (String) v);
     
        if (userSortBy == SortBy.DAY || !config.isPaging() || config.getIndex() == -1) {
            index = config.getIndex();
        } else {
            index = config.getIndex() + (offset);
        }
    }
    
    public SearchResult execute() {
        if (!executeQuery) {
            return new BasicSearchResult(this);
        }
        
        final String waitOn = config.getWaitOn();
        
        if (waitOn != null) {
            LOG.debug("Waiting on: " + waitOn);
            try {
                wosr = (FastSearchResult) context.getRunningQuery().getSearchResult(waitOn);
                if (wosr == null) {
                    throw new NullPointerException("wait-on result is null");
                }
            } catch (Exception e) {
                LOG.error(e);
                return new BasicSearchResult(this);
            }
        }
        
        if (waitOn != null) {
            /* Abort if navigator gave no result */
            if (wosr.getHitCount() == 0) {
                executeQuery = false;
            }
        }
        
        if (executeQuery && index > 0) {
            /* Abort all but the first command on one-command-searches */
            
            /* If using channel navigator and sorting by channels */
            if (userSortBy == SortBy.CHANNEL) {
                if (getParameters().get("nav_channels") != null || wosr.getModifiers("channels").size() < index + 1 ) {
                    executeQuery = false;
                }
            }
            
            /* If using category navigator and sorting by category */
            if (userSortBy == SortBy.CATEGORY) {
                if (getParameters().get("nav_categories") != null || wosr.getModifiers("categories").size() < index + 1) {
                    executeQuery = false;
                }
            }
            
            /* If using day navigator and sorting on day */
            if (userSortBy == SortBy.DAY) {
                if (getParameters().get("day") != null) { 
                    executeQuery = false;
                }
            }
        }
        
        if (executeQuery == false) {
            return new BasicSearchResult(this);
        }        
        
        return super.execute();
    }
    
    public String getAdditionalFilter() {
        LOG.debug("getAdditionalFilter()");
        final int day = getParameters().containsKey("day") ? Integer.parseInt((String) getParameters().get("day")) : 0;
        final StringBuilder filter = new StringBuilder();
        
        Calendar cal = Calendar.getInstance();
       
         /* Adjust time to selected day */
        cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY * (SortBy.DAY == userSortBy ? index : day));
        
        if (userSortBy == SortBy.CHANNEL) {
            /* Starttime greater than now() or 05:00 on selected day */
            final String dateFmt = day == 0 ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'05:00:00'Z'";
            filter.append("+starttime:>").append(new SimpleDateFormat(dateFmt).format(cal.getTime())).append(" ");
            
            /* Starttime less than 05:00 the next day */
            cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY);
            filter.append("+starttime:<").append(new SimpleDateFormat("yyyy-MM-dd'T05:00:00Z'").format(cal.getTime())).append(" ");
            
            /* Use channels navigator in waitOn command */
            if (config.getWaitOn() != null) {
                final Modifier modifier = (Modifier) wosr.getModifiers("channels").get(index);
                final Navigator navigator = modifier.getNavigator();
                filter.append("+").append(navigator.getField()).append(":").append(modifier.getName()).append(" ");
            }
        } else if (userSortBy == SortBy.DAY) {
            /* Starttime greater than now() or 05:00 on selected day */
            final String dateFmt = index == 0 ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'05:00:00'Z'";
            filter.append("+starttime:>").append(new SimpleDateFormat(dateFmt).format(cal.getTime())).append(" ");
        
            /* Starttime less than 05:00 the next day or less than 05:00 seven days from now for the navigator */
            if (config.getWaitOn() != null) {
                cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY);
            } else {
                cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY * 7);
            }
            filter.append("+starttime:<").append(new SimpleDateFormat("yyyy-MM-dd'T05:00:00Z'").format(cal.getTime())).append(" ");
            
            /* Use channels navigator to add filter for top 5 channels */
            if (config.getWaitOn() != null && wosr.getModifiers("channels").size() > 0) {
                filter.append("+(");
                final int maxIdx = wosr.getModifiers("channels").size() < 5 ? wosr.getModifiers("channels").size() : 5;
                for (Modifier modifier : wosr.getModifiers("channels").subList(0, maxIdx)) {
                    final Navigator navigator = modifier.getNavigator();
                    filter.append(navigator.getField()).append(":").append(modifier.getName()).append(" ");
                }
                filter.append(")");
            }
            
        } else if (userSortBy == SortBy.CATEGORY) {
            /* Starttime greater than now() or 05:00 on selected day */
            final String dateFmt = day == 0 ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'05:00:00'Z'";
            filter.append("+starttime:>").append(new SimpleDateFormat(dateFmt).format(cal.getTime())).append(" ");
            
            /* Starttime less than 05:00 the next day */
            cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY);
            filter.append("+starttime:<").append(new SimpleDateFormat("yyyy-MM-dd'T05:00:00Z'").format(cal.getTime())).append(" ");
            
            /* Use categories navigator to select categories to display */
            if (config.getWaitOn() != null) {
                final Modifier modifier = (Modifier) wosr.getModifiers("categories").get(index);
                final Navigator navigator = modifier.getNavigator();
                filter.append("+").append(navigator.getField()).append(":").append(modifier.getName()).append(" ");
                
                /* Only include the top 5 channels */
                filter.append("+(");
                final int maxIdx = wosr.getModifiers("channels").size() < 5 ? wosr.getModifiers("channels").size() : 5;
                for (Modifier channelModifier : wosr.getModifiers("channels").subList(0, maxIdx)) {
                    final Navigator channelNavigator = channelModifier.getNavigator();
                    filter.append(channelNavigator.getField()).append(":").append(channelModifier.getName()).append(" ");
                }
                filter.append(")");
            }
    
        }
        
        return filter.toString();
    }
    
    /** Return offset to use when collecting results.
     * @return Will always return 0
     */
    protected int getCurrentOffset(final int i) {
        return 0;
    }
}
