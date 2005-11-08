package no.schibstedsok.front.searchportal.query;


/**
 * Query query can be applied to queries before sending them away to
 * indices or other sources for further processing.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface QueryTransformer {


    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    String getTransformedQuery(String originalQuery);

    /**
     * Set filter for this query. By analizing the query we can
     * set dynamic filter. For example a special sort order or specify contentsource
     *
     * Example to add docdatetime argument
     *
     * +docdatetime:>2005-10-28
     *
     * @return filterstring
     */
    String getFilter(String originalQuery);

}
