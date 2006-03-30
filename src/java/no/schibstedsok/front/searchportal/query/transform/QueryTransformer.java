// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.transform;





import java.util.Map;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.QueryContext;
import no.schibstedsok.front.searchportal.query.Visitor;
import no.schibstedsok.front.searchportal.site.SiteContext;


/**
 * Query query can be applied to queries before sending them away to
 * indices or other sources for further processing.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface QueryTransformer extends Visitor, Cloneable{

    public interface Context extends QueryContext, ResourceContext, SiteContext {

        /**
         * @deprecated use getTransformedTerms() instead
         **/
        String getTransformedQuery();
        /** Get the terms with their current transformed representations.
         *
         **/
        Map<Clause,String> getTransformedTerms();
    }

    void setContext(final Context cxt);

    /**
     * Add keywords to query to get better searchresults
     *
     * @deprecated use the visitor pattern instead via visit(Object)
     * @param originalQuery
     * @return
     */
    String getTransformedQuery();

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
    String getFilter(Map parameters);

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
    String getFilter();
    
    /** Force public implementation of Clonable. **/
    Object clone() throws CloneNotSupportedException ;
}
