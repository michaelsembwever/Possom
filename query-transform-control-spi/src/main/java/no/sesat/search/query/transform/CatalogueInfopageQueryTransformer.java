/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.query.transform;


/**
 * Add iypcompanyid to the front of the original query, which is a companyid.
 *
 * @author <a href="mailto:daniele@conduct.no">Daniel Engfeldt</a>
 * @version $Revision:$
 */
public final class CatalogueInfopageQueryTransformer extends AbstractQueryTransformer {

    /**
     *
     * @param config
     */
    public CatalogueInfopageQueryTransformer(final QueryTransformerConfig config){}

    public String getTransformedQuery() {
        final String originalQuery = getContext().getTransformedQuery();
        return "iypcompanyid:" + originalQuery;
    }
}
