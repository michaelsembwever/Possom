/*
 * Copyright (2008) Schibsted ASA
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

import no.sesat.search.query.BinaryClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.UnaryClause;
import no.sesat.search.query.transform.AbstractQueryTransformer;

/** Default initial QueryTransformer that fills out the transformedTerms map.
 *
 * Is not thread safe.
 * Does not use the QueryTransformerConfig.
 * Escapes through the context the ':' inbetween non-filter fields and their terms.
 *
 * @todo implement NotClause and AndNotClause.
 *
 * @version $Id$
 */
public class DefaultInitialisationQueryTransformer extends AbstractQueryTransformer{

    public DefaultInitialisationQueryTransformer(QueryTransformerConfig config) {
        super();
    }

    protected void visitImpl(final LeafClause clause) {

        if (null == getContext().getTransformedTerms().get(clause)) {
            if (null != clause.getField()) {
                if (null == getContext().getFieldFilter(clause)) {

                    // a field that isn't a filter so it must be escaped
                    getContext().getTransformedTerms().put(
                            clause,
                            clause.getField()  + getContext().escape(":") + clause.getTerm());

                } else {

                    // this would be a valid filter so blank it
                    getContext().getTransformedTerms().put(clause, "");
                }
            } else {
                getContext().getTransformedTerms().put(clause, clause.getTerm());
            }
        }
    }

    protected void visitImpl(final UnaryClause clause) {

        clause.getFirstClause().accept(this);
    }

    protected void visitImpl(final BinaryClause clause) {

        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }


}
