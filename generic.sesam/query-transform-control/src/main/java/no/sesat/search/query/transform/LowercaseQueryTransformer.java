/*
 * Copyright (2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 */
package no.sesat.search.query.transform;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import no.sesat.search.query.Clause;
import no.sesat.search.query.BinaryClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.UnaryClause;
import org.apache.log4j.Logger;

/**
 * @see LowercaseQueryTransformerConfig
 *
 * @version $Id$
 */
public final class LowercaseQueryTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(LowercaseQueryTransformer.class);

    public LowercaseQueryTransformer(final QueryTransformerConfig config){}

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {

        final String term = getTransformedTerms().get(clause);

        if(null != term && term.length()>0){

            getTransformedTerms().put(
                    clause,
                    getTransformedTerms().get(clause).toLowerCase(getContext().getSite().getLocale()));
        }
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final BinaryClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final UnaryClause clause) {
        clause.getFirstClause().accept(this);
    }

    private Map<Clause,String> getTransformedTerms() {
        return getContext().getTransformedTerms();
    }

}
