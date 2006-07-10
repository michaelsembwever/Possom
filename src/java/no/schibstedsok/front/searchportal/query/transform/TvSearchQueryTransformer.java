// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TvSearcQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author ajamtli
 * @version $Id: TvQueryTransformer.java 2918 2006-05-15 11:40:31Z magnuse $
 */
public final class TvSearchQueryTransformer extends AbstractQueryTransformer {

    private static final Log LOG = LogFactory.getLog(TvSearchQueryTransformer.class);
  
    private boolean withEndtime;
    
    /**
     * Set time window
     * @return filter for time window to search from
     */
    public String getFilter(final Map parameters) {

        final boolean blankQuery = getContext().getQuery().isBlank();
        
        final boolean navChannels = parameters.get("nav_channels") != null ? true : false;
        final boolean navDays = parameters.get("nav_days") != null ? true : false;
        final boolean navCategories = parameters.get("nav_categories") != null ? true : false;
        final boolean noNav = !(navChannels || navDays || navCategories); 

        final boolean isRSS = parameters.get("output") != null && parameters.get("output").equals("rss");
        
        if (parameters.get("userSortBy") == null && !blankQuery) {
            parameters.put("userSortBy", "day");
        }
        
        final String sortByString = parameters.get("userSortBy") != null ? (String) parameters.get("userSortBy") : "channel";
        
        final boolean sortByChannel = "channel".equals(sortByString);
        final boolean sortByDay = "day".equals(sortByString);
        final boolean sortByCategory = "category".equals(sortByString);
        
        Calendar cal = Calendar.getInstance();
        final StringBuilder filter = new StringBuilder();
        filter.append("+endtime:>");
        filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime()));
        filter.append("");
        
        if (blankQuery && !isRSS) {
            if (noNav 
                    || (navDays && !navChannels && !sortByDay)
                    || (navChannels && !sortByChannel)) {
                filter.append(" +igeneric3:>17 ");
            }
            if (noNav && sortByDay) {
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60);
                filter.append(" +(starttime:<");
                filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(todayCal.getTime()));
                filter.append(" or igeneric3:<19)");
            }
        }
        
        if (getWithEndtime() && parameters.get("nav_days") == null) {
            if (sortByChannel || sortByCategory) {
                filter.append(" +starttime:<");
                cal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60 * 24);
                filter.append(new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'").format(cal.getTime()));
            } else if (sortByDay) {
                filter.append(" +starttime:<");
                cal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60 * 24 * 7);
                filter.append(new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'").format(cal.getTime()));
            }
        } else {
            cal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60 * 24 * 7);
            filter.append(" +starttime:<");
            filter.append(new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'").format(cal.getTime()));
        }
        return filter.toString();
    }
    
    public boolean getWithEndtime() {
        return withEndtime;
    }
    
    public void setWithEndtime(boolean withEndtime) {
        this.withEndtime = withEndtime;
    }
}
