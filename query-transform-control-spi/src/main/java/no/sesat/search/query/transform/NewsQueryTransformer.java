/* Copyright (2006-2007) Schibsted ASA
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


/**
 * NewsTransformer is part of no.sesat.search.query
 *
 *
 * @version 0.1
 * @vesrion $Revision: 3359 $, $Author: mickw $, $Date: 2006-08-03 10:13:22 +0200 (Thu, 03 Aug 2006) $
 */
public final class NewsQueryTransformer extends AbstractQueryTransformer implements QueryTransformer {


    /**
     *
     * @param config
     */
    public NewsQueryTransformer(final QueryTransformerConfig config){
    }


    /**
     * Add keywords to query to get better searchresults
     *
     * @param originalQuery
     * @return
     */
    public String getTransformedQuery() {

        final String originalQuery = getContext().getTransformedQuery();

       return originalQuery;
    }

    /**
     * Set filter for thiw query.
     * Example to add docdatetime argument
     * <p/>
     * +docdatetime:>2005-10-28
     *
     * @return filterstring
     */
    public String getFilter() {

        final String origQuery = getContext().getTransformedQuery();

        if (origQuery == null) {
            throw new IllegalArgumentException("setQuery not called with minimum empty query");
        }

        if ("".equals(origQuery.trim())) {
            return " +size:>0 ";
        }

        return null;
    }


}
