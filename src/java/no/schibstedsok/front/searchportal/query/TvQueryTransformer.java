package no.schibstedsok.front.searchportal.query;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TvQueryTransformer is part of no.schibstedsok.front.searchportal.query
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class TvQueryTransformer extends AbstractQueryTransformer {


    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    public String getTransformedQuery(final Context cxt) {
        
        final String originalQuery = cxt.getQueryString();

        return originalQuery;
    }


    /**
     * Set docdatetime > current date
     * @return docdatetime:>[FORMATTED DATE]
     */
    public String getFilter(final Context cxt) {
        
        final String originalQuery = cxt.getQueryString();


        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return "+expiresdate:>" + sdf.format(new Date());
    }
}
