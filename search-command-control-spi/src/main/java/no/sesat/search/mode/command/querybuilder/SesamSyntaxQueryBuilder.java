/*Copyright (2008) Schibsted Søk AS
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

import no.sesat.search.mode.config.querybuilder.InfixQueryBuilderConfig;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.XorClause;

/** Query builder for creating a query syntax similar to sesam's own.
 *
 * Is not thread safe.
 * It does not use the QueryBuilderConfig.
 *
 * Currently is basically a PrefixQueryBuilder with OrClauses wrapped in () parenthesis.
 *
 * @version $Id$
 */
public class SesamSyntaxQueryBuilder extends InfixQueryBuilder{

    // Constants -----------------------------------------------------

    private static final QueryBuilderConfig SESAM_SYNTAX_CONFIG = new InfixQueryBuilderConfig(
            "",
            "AND",
            "",
            "NOT ",
            true,
            false,
            false);

    //private static final Logger LOG = Logger.getLogger(SesamSyntaxQueryBuilder.class);


    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public SesamSyntaxQueryBuilder(final Context cxt) {
        super(cxt, SESAM_SYNTAX_CONFIG);
    }

    // AbstractReflectionVisitor implementation ----------------------------------------------

    private boolean insideOr = false;

    @Override
    protected void visitImpl(final OrClause clause) {

        boolean wasInside = insideOr;
        if (!insideOr) {
            appendToQueryRepresentation('(');
        }
        insideOr = true;
        super.visitImpl(clause);
        insideOr = wasInside;
        if (!insideOr) {
            appendToQueryRepresentation(')');
        }
    }

    /** Overridden so to avoid visiting any FULLNAME_ON_LEFT.
     *
     * @param clause @{@inheritDoc}
     */
    @Override
    protected void visitImpl(final XorClause clause) {

        switch (clause.getHint()) {
            case FULLNAME_ON_LEFT:
                clause.getSecondClause().accept(this);
                break;
            default:
                super.visitImpl( clause);
        }
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
