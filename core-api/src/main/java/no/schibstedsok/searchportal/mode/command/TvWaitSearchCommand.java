/*
 * TvWaitSearchCommand.java
 *
 * Created on 26 October 2006, 14:03
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import no.schibstedsok.searchportal.mode.config.TvWaitSearchConfiguration;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.log4j.Logger;

/**
 *
 * @author andersjj
 */
public final class TvWaitSearchCommand extends AbstractSimpleFastSearchCommand {
    
    // Constants -----------------------------------------------------
    
    /** Logger */
    private static final Logger LOG = Logger.getLogger(TvWaitSearchCommand.class);
    
    /** Millis in day */
    private static final int MILLIS_IN_DAY = 86400000;
    
    /** Sort options */
    private static enum SortBy {
        CHANNEL,
        DAY,
        CATEGORY;
    }
    
    // Attributes ----------------------------------------------------
    
    /** Search command configuration */
    private final TvWaitSearchConfiguration config;
    
    /** Wait on search result */
    private FastSearchResult wosr;

    /** Index to use when creating filters */
    private final int index;
        
    /** Sort mode */
    private final SortBy userSortBy; 
   
    /** Blank query */
    private final boolean blankQuery;
    
    /** Execute query */
    private boolean executeQuery = true;

    /** Use all channels */
    private boolean useAllChannels = false;
    
    // Static --------------------------------------------------------

    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of TvWaitSearchCommand */
    public TvWaitSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
        
        blankQuery = cxt.getQuery().isBlank();
        this.config = (TvWaitSearchConfiguration) cxt.getSearchConfiguration();
        
        final String defaultUserSortBy = blankQuery ? "CHANNEL" : "DAY";
        final String usbp = getParameters().containsKey("userSortBy") 
                ? ((String) getParameters().get("userSortBy")).toUpperCase() 
                : defaultUserSortBy;
        
        SortBy tmpUserSortBy = null;
        try {
            tmpUserSortBy = SortBy.valueOf(usbp);
        } catch (IllegalArgumentException e) {
            tmpUserSortBy = SortBy.valueOf(defaultUserSortBy);
        } finally {
            userSortBy = tmpUserSortBy;
        }
        
        final int offset = getParameter("offset").length() > 0 ? Integer.parseInt(getParameter("offset")) : 0;

        if (userSortBy == SortBy.DAY || !config.isPaging() || config.getIndex() == -1) {
            index = config.getIndex();
        } else {
            index = config.getIndex() + (offset);
        }
    
        useAllChannels = getParameter("allChannels") != null && "true".equals(getParameter("allChannels"));
    }
    
    
    // Public --------------------------------------------------------
    
    public SearchResult execute() {
        
        if (!executeQuery) {
            return new FastSearchResult(this);
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
                return new FastSearchResult(this);
            }
        }
        
        if (waitOn != null) {
            /* Abort if navigator gave no result */
            if (wosr.getHitCount() == 0) {
                executeQuery = false;
            }
        }
        
        if (waitOn != null && executeQuery && index > 0 && wosr.getHitCount() > 0) {
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

                
        /* If navigator root command and blankQuery, set default navigator */
        if (executeQuery && blankQuery && !getParameters().containsKey("nav_channelcategories")) {
            if (config.getUseMyChannels() && getParameter("myChannels") != null && getParameter("myChannels").length() > 0) {
                final String myChannels = getParameter("myChannels");
                if (myChannels.split(",").length > 50) {
                    executeQuery = addChannelCategoryNavigator();
                }
            } else {
                executeQuery = addChannelCategoryNavigator();
            }
        }
        
        if (executeQuery && config.getUseMyChannels() && getParameters().containsKey("setMyChannels")) {
            executeQuery = false;
        }
        
        if (executeQuery == false) {
            return new FastSearchResult(this);
        }        
        
        SearchResult sr = super.execute();
        if (sr.getHitCount() > 0) {
            SearchResultItem sri = sr.getResults().get(0);
            String startTime = sri.getField("starttime");
            String endTime = sri.getField("endtime");
            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            try {
                Date st = sdf.parse(startTime);
                Date et = sdf.parse(endTime);

                if (now.compareTo(st) >= 0 && now.compareTo(et) < 0) {
                    sri.addField("current", "true");
                }
            } catch (ParseException e) {}
        }
        return sr;
    }
    
    
    public String getAdditionalFilter() {
        
        LOG.debug("getAdditionalFilter()");
        final int day = getParameters().containsKey("day") ? Integer.parseInt((String) getParameters().get("day")) : 0;
        final StringBuilder filter = new StringBuilder();
        
        Calendar cal = Calendar.getInstance();
       
         /* Adjust time to selected day */
        final int adjustment = index < 0 ? 0 : index;
        cal.setTimeInMillis(cal.getTimeInMillis() + MILLIS_IN_DAY * (SortBy.DAY == userSortBy && getParameters().get("day") == null ? adjustment : day));
        
        if (userSortBy == SortBy.CHANNEL) {
            /* Starttime greater than now() or 05:00 on selected day */
            final String dateFmt = day == 0 ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'05:00:00'Z'";
            filter.append("+endtime:>").append(new SimpleDateFormat(dateFmt).format(cal.getTime())).append(" ");
            
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
            final String dateFmt = index < 1 ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'05:00:00'Z'";
            filter.append("+endtime:>").append(new SimpleDateFormat(dateFmt).format(cal.getTime())).append(" ");
        
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
            filter.append("+endtime:>").append(new SimpleDateFormat(dateFmt).format(cal.getTime())).append(" ");
            
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
                filter.append(") ");
            }
    
        }
        
        if (config.getUseMyChannels() && !useAllChannels && getParameter("myChannels") != null && getParameter("myChannels").length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("+(");
            for (String str : getParameter("myChannels").split(",")) {
                sb.append(" sgeneric5nav:'");
                sb.append(str);
                sb.append("'");
            }
            filter.append(sb);
            filter.append(") ");
        }
        
        return filter.toString();
    }
    
    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    
    /** Return offset to use when collecting results.
     * @return Will always return 0
     */
    protected int getCurrentOffset(final int i) {
        
        return 0;
    }
    
    
    // Private -------------------------------------------------------
    
    
    private final boolean addChannelCategoryNavigator() {
        
        final Navigator navigator = getNavigators().get("channelcategories");
        if (navigator == null) {
            return false;
        }            
        
        getParameters().put("nav_channelcategories", navigator.getName());
        getParameters().put(navigator.getField(), "Norske");
        
        return true;
    }
    
    // Inner classes -------------------------------------------------

}
