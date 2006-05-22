// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WebTvQueryTransformer is part of no.schibstedsok.front.searchportal.query.transform
 *
 * @author ajamtli
 * @version $Id: TvQueryTransformer.java 2918 2006-05-15 11:40:31Z magnuse $
 */
public final class WebTvQueryTransformer extends AbstractQueryTransformer {

    private static final Log LOG = LogFactory.getLog(WebTvQueryTransformer.class);
    
    /**
     * Set starttime < current datetime
     * @return starttime:<[FORMATTED DATETIME]
     */
    public String getFilter() {

        final String origQuery = getContext().getQuery().getQueryString();

        final StringBuilder filter = new StringBuilder();
        
        filter.append("+starttime:<");
        filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));
        
        return filter.toString();
    }
}
