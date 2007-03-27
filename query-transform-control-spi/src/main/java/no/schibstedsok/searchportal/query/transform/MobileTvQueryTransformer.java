// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * MobileTvQueryTransformer is part of no.schibstedsok.front.searchportal.query.
 * @deprecated This class is redundant. TODO it the same way as sesam.no does it.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id: TvQueryTransformer.java 3286 2006-07-06 11:05:16Z ajamtli $
 */
public final class MobileTvQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(MobileTvQueryTransformer.class);

/**
     *
     * @param config
     */
    public MobileTvQueryTransformer(final QueryTransformerConfig config){}

    
    /**
     * Set docdatetime > current date.
     * @return docdatetime:>[FORMATTED DATE]
     */
    public String getFilter() {

        final String origQuery = getContext().getQuery().getQueryString();

        final StringBuilder filter = new StringBuilder();

        // Special case to choose channel. Mobile only.
        // TODO: we're now using tv data from two different indices. The new
        // is used by web & and the old one is used by mobile.
        if (origQuery.startsWith("tv_")) {
           filter.append("+wpfornavn:^");
           filter.append(origQuery.substring(3));
           filter.append("$ ");
           filter.append("+tvstarttime:>");
           filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));

        } else {

            filter.append("+starttime:>");
            filter.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));

            filter.append(" +starttime:<");
            filter.append(new SimpleDateFormat("yyyy-MM-dd'T'23:59:59'Z'").format(new Date()));


        }
        return filter.toString();
    }
}
