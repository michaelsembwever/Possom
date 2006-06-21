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
        final String sortBy = parameters.get("userSortBy") != null ? (String) parameters.get("userSortBy") : "channel";
        
        Calendar cal = Calendar.getInstance();
        final StringBuilder filter = new StringBuilder();
        filter.append("+starttime:>");
        filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime()));
        filter.append("");

        if (blankQuery) {
            if (sortBy.equals("channel") || sortBy.equals("category")) {
                filter.append(" +starttime:<");
                cal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60 * 24);
                filter.append(new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'").format(cal.getTime()));
            } else if (sortBy.equals("day")) {
                filter.append(" +starttime:<");
                cal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60 * 24 * 7);
                filter.append(new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'").format(cal.getTime()));
            }
        }
        if (blankQuery && getWithEndtime()) {


            filter.append(" +endtime:<");
            if (sortBy.equals("day")) {
                /* Add one week */
                cal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60 * 24 * 7);
            } else {
                /* Add one day */
                cal.setTimeInMillis(cal.getTimeInMillis() + 1000 * 60 * 60 * 24);
            }
            filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime()));
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
