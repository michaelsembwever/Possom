/* Copyright (2007) Schibsted Søk AS
 *   This file is part of SESAT.
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

import no.sesat.search.query.Clause;
import org.apache.log4j.Logger;

public class NewsMediumQueryTransformer extends AbstractQueryTransformer {
    private static final Logger LOG = Logger.getLogger(NewsMediumQueryTransformer.class);
    private NewsMediumQueryTransformerConfig config;

    public NewsMediumQueryTransformer(final QueryTransformerConfig config) {
        this.config = (NewsMediumQueryTransformerConfig) config;
    }

    public void visitImpl(final Clause clause) {
        String medium = (String) getContext().getDataModel().getJunkYard().getValue(config.getMediumParameter());
        final String originalQuery = getTransformedTermsQuery();
        if (!NewsMediumQueryTransformerConfig.ALL_MEDIUMS.equals(medium) && originalQuery.length() > 0) {
            if (medium == null || medium.length() == 0) {
                medium = config.getDefaultMedium();
            }
            StringBuilder query = new StringBuilder(originalQuery);
            query.insert(0, "and(");
            query.append(", ").append(config.getMediumPrefix()).append(':').append(medium).append(')');
            for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
                getContext().getTransformedTerms().put(keyClause, "");
            }
            LOG.debug("Transformed query is: '" + query.toString() + "'");
            getContext().getTransformedTerms().put(getContext().getQuery().getFirstLeafClause(), query.toString());
        }
    }

    private String getTransformedTermsQuery() {
        StringBuilder query = new StringBuilder();
        for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
            query.append(getContext().getTransformedTerms().get(keyClause));
        }
        return query.toString();
    }

}
