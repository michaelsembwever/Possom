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
 * NowQueryTransformer can be used to add before or after current time filter
 *
 * @author ajamtli
 * @version $Id$
 */
public final class NowQueryTransformer extends AbstractQueryTransformer {

    private static final Log LOG = LogFactory.getLog(NowQueryTransformer.class);
  
    private String prefix;
    
    /**
     * Set time window
     * @return filter for time window to search from
     */
    public String getFilter(final Map parameters) {

        Calendar cal = Calendar.getInstance();
        final StringBuilder filter = new StringBuilder();
        filter.append(prefix);
        filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cal.getTime()));
        //filter.append("");
        
        return filter.toString();
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
