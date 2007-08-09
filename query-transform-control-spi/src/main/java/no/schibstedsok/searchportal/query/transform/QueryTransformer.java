/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
package no.schibstedsok.searchportal.query.transform;


import no.schibstedsok.searchportal.datamodel.DataModelContext;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.QueryContext;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.ResourceContext;
import org.w3c.dom.Element;

import java.util.Map;


/**
 * Query query can be applied to queries before sending them away to
 * indices or other sources for further processing.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 3829 $</tt>
 */
public interface QueryTransformer extends Visitor{

    /**
     *
     */
    public interface Context extends QueryContext, ResourceContext, SiteContext, DataModelContext {

        /**
         * @return
         * @deprecated use getTransformedTerms() instead
         */
        String getTransformedQuery();

        /**
         * Get the terms with their current transformed representations.
         * @return
         */
        Map<Clause, String> getTransformedTerms();

        /**
         * For evaluation acitions on individual (or the whole query) terms.
         * @return
         */
        TokenEvaluationEngine getTokenEvaluationEngine();

        /**
         * QueryTransformers must follow the same XorClause hints as the search command. *
         * @param visitor
         * @param clause
         */
        void visitXorClause(Visitor visitor, XorClause clause);

        /**
         * QueryTransformers needs information about supported field filters. *
         * @param clause
         * @return
         */
        String getFieldFilter(LeafClause clause);
    }

    /**
     * TODO comment me. *
     * @param cxt
     */
    void setContext(final Context cxt);

    /**
     * Add keywords to query to get better searchresults
     *
     * @return
     * @deprecated use the visitor pattern instead via visit(Object)
     */
    String getTransformedQuery();

    /**
     * Set filter for this query. By analizing the query we can
     * set dynamic filter. For example a special sort order or specify contentsource
     * <p/>
     * Example to add docdatetime argument
     * <p/>
     * +docdatetime:>2005-10-28
     *
     * @param parameters
     * @return filterstring
     */
    String getFilter(Map parameters);

    /**
     * Set filter for this query. By analizing the query we can
     * set dynamic filter. For example a special sort order or specify contentsource
     * <p/>
     * Example to add docdatetime argument
     * <p/>
     * +docdatetime:>2005-10-28
     *
     * @return filterstring
     */
    String getFilter();

}
