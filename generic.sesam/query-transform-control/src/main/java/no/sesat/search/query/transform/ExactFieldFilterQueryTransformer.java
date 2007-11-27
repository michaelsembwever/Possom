/* Copyright (2007) Schibsted Søk AS
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

import no.sesat.search.query.Clause;
import no.sesat.search.datamodel.generic.StringDataObject;
import org.apache.log4j.Logger;

/**
 * Adds a filter caluse to a ESP5 query to exactly match a value.
 *
 *
 * @author Geir H. Pettersen (T-Rank)
 */
public class ExactFieldFilterQueryTransformer extends AbstractQueryTransformer {

    private ExactFieldFilterQueryTransformerConfig config;
    private static final Logger LOG = Logger.getLogger(ExactFieldFilterQueryTransformer.class);

    public ExactFieldFilterQueryTransformer(QueryTransformerConfig config) {
        this.config = (ExactFieldFilterQueryTransformerConfig) config;
    }

    public void visitImpl(final Clause clause) {
        final String originalQuery = getTransformedTermsQuery();
        final String filterField = config.getFilterField();
        StringDataObject matchObject = getContext().getDataModel().getParameters().getValue(config.getFilterParameter());
        if (matchObject != null) {
            boolean andInserted = false;
            String matchValue = matchObject.getString().replace('?', ' ');
            StringBuilder query = new StringBuilder(originalQuery);
            if (query.length() > 0) {
                query.insert(0, "and(");
                query.append(", ");
                andInserted = true;
            }
            query.append(filterField).append(':');
            if (config.isUseEquals()) {
                query.append("equals");
            }
            query.append('(');
            query.append("\"").append(matchValue).append("\"");
            query.append(')');
            if (andInserted) {
                query.append(')');
            }
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
