// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TvQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @vesrion $Revision$, $Author$, $Date$
 */
public final class TvQueryTransformer extends AbstractQueryTransformer {

    private static final Log LOG = LogFactory.getLog(TvQueryTransformer.class);
    
    private static final int REG_EXP_OPTIONS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern TV_IDAG = Pattern.compile("på\\s+tv\\s+i\\s*dag$", REG_EXP_OPTIONS);

    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    public String getTransformedQuery() {

        final String transformedQuery = getContext().getTransformedQuery();
        return TV_IDAG.matcher(transformedQuery).replaceAll("");
    }


    /**
     * Set docdatetime > current date
     * @return docdatetime:>[FORMATTED DATE]
     */
    public String getFilter() {

        final String origQuery = getContext().getQuery().getQueryString();

        LOG.debug("TVVVVVV");

        return origQuery.length() > 0
                ? "+tvendtime:>" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date())
                : "";
    }
}
