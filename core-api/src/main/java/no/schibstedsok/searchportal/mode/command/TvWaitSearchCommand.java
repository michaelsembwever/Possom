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
        LOG.debug("Creating TvWaitSearchCommand");
        this.config = (TvWaitSearchConfiguration) cxt.getSearchConfiguration();
        final String usbp = getParameters().containsKey("userSortBy") ? ((String) getParameters().get("userSortBy")).toUpperCase() : "CHANNEL";
        userSortBy = SortBy.valueOf(usbp);
        blankQuery = cxt.getQuery().isBlank();
    }
    
    public SearchResult execute() {
        if (!executeQuery) {
            return new BasicSearchResult(this);
        }
        final int index = config.getIndex();
        
        if (index > 0) {
            /* Abort all but the first command on one-command-searches */
            
            /* If using channel navigator and sorting by channels */
            if (userSortBy == SortBy.CHANNEL && getParameters().get("nav_channels") != null) {
                executeQuery = false;
            }
            
            /* If using category navigator and sorting by category */
            if (userSortBy == SortBy.CATEGORY && getParameters().get("nav_categories") != null) {
                executeQuery = false;
            }
            
            /* If using day navigator and sorting on day */
            if (userSortBy == SortBy.DAY && getParameters().get("day") != null) {
                executeQuery = false;
            }
            
            if (executeQuery == false) {
                return new BasicSearchResult(this);
            }
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
        LOG.debug("EXECUTING TvWaitSearchCommand");
        return super.execute();
    }
    
    public String getAdditionalFilter() {
        LOG.debug("getAdditionalFilter()");
        final int day = getParameters().containsKey("day") ? Integer.parseInt((String) getParameters().get("day")) - 1 : 0;
        final StringBuilder filter = new StringBuilder();
        final int index = config.getIndex();
        
        if (index > 0) {
            /* Abort all but the first command on one-command-searches */
            
            /* If using channel navigator and sorting by channels */
            if (userSortBy == SortBy.CHANNEL && getParameters().get("nav_channels") != null) {
                executeQuery = false;
            }
            
            /* If using category navigator and sorting by category */
            if (userSortBy == SortBy.CATEGORY && getParameters().get("nav_categories") != null) {
                executeQuery = false;
            }
            
            /* If using day navigator and sorting on day */
            if (userSortBy == SortBy.DAY && getParameters().get("day") != null) {
                executeQuery = false;
            }
            
            if (executeQuery == false) {
                return "";
            }
        }
        
        Calendar cal = Calendar.getInstance();
        
        if (userSortBy == SortBy.CHANNEL) {
            /* Adjust time to selected day */
            cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY * day);
            
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
            /* Adjust time to selected day */
            cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY * index);
            
            /* Startime greater than now() or 05:00 selected day */
            if (index == 0) {
                filter.append("+starttime:>").append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime())).append(" ");
            } else {
                filter.append("+starttime:>").append(new SimpleDateFormat("yyyy-MM-dd'T05:00:00Z'").format(cal.getTime())).append(" ");
            }
        
            /* Starttime less than 05:00 the next day */
            cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY);
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
            /* Adjust time to selected day */
            cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY * day);
            
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
}
