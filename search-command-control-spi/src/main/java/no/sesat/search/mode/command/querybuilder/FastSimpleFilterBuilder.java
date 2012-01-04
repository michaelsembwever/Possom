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
package no.sesat.search.mode.command.querybuilder;

import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.LeafClause;
import org.apache.log4j.Logger;

/**
 *
 * @version $Id$
 */
public final class FastSimpleFilterBuilder extends BaseFilterBuilder{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FastSimpleFilterBuilder.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public FastSimpleFilterBuilder(final Context cxt, final QueryBuilderConfig config) {
        super(cxt, config);
    }

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    @Override
    protected void appendFilter(final LeafClause clause) {

        final String fieldAs = getContext().getFieldFilter(clause);
        String term = clause.getTerm();

        if ("site".equals(fieldAs)) {
            term = term.replaceAll("\"", "");
        }

        appendToQueryRepresentation(" +" + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------


}
