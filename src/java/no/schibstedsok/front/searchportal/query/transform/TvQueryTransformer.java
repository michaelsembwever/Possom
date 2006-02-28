// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TvQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @vesrion $Revision$, $Author$, $Date$
 */
public class TvQueryTransformer extends AbstractQueryTransformer {

    private static Log log = LogFactory.getLog(TvQueryTransformer.class);

    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    public String getTransformedQuery(final Context cxt) {

        final String originalQuery = cxt.getTransformedQuery();

        if (originalQuery.matches("^tv$")) {
            return "";
        }

        if (originalQuery.matches("p.*\\stv\\sidag$")) {
            return "";
        }

        return originalQuery;
    }


    /**
     * Set docdatetime > current date
     * @return docdatetime:>[FORMATTED DATE]
     */
    public String getFilter(final Context cxt) {

        final String originalQuery = cxt.getTransformedQuery();

        log.debug("TVVVVVV");

        final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return "+tvendtime:>" + sdf.format(new Date());
    }
}
