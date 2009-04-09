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
package no.sesat.search.mode.command.querybuilder;

import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.LeafClause;
import org.apache.log4j.Logger;

/**
 *
 * @version $Id$
 */
public final class FastAdvancedFilterBuilder extends BaseFilterBuilder{

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FastAdvancedFilterBuilder.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public FastAdvancedFilterBuilder(final Context cxt, final QueryBuilderConfig config) {
        super(cxt, config);
    }

    // Public --------------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    @Override
    protected void appendFilter(LeafClause clause) {

        final String fieldAs = getContext().getFieldFilter(clause);
        String term = clause.getTerm();

        if ("site".equals(fieldAs)) {
            term = term.replaceAll("\"", "");
        }

        appendToQueryRepresentation(" AND " + (fieldAs.length() > 0 ? fieldAs + ':' + term : term));

    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------


}
