/* Copyright (2006-2009) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.query.transform;


import no.sesat.search.datamodel.DataModelContext;
import no.sesat.search.query.Clause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.QueryContext;
import no.sesat.commons.visitor.Visitor;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.ResourceContext;
import org.w3c.dom.Element;

import java.util.Map;
import no.sesat.search.mode.config.SearchConfiguration;


/**
 * Query query can be applied to queries before sending them away to
 * indices or other sources for further processing.
 *
 *
 * @version <tt>$Id$</tt>
 */
public interface QueryTransformer extends Visitor{

    /**
     * The context any QueryTransformer must work through.
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
        /** Escape the word.
         * The word need not be reserved or require escaping but should be escaped anyway.
         *
         * @param word
         * @return escaped version of the word
         */
        String escape(String word);
    }

    /**
     * Provide the context. Since it cannot be provided through the constructor.
     *
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
     * @deprecated all functionality should be provided by getFilter() with any access to parameters via the datamodel
     *
     * @param parameters the datamodel's parameters
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
