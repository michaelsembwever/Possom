/*Copyright (2012) Schibsted ASA
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

import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.mode.config.querybuilder.InfixQueryBuilderConfig;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.LeafClause;
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
            "-",
            true,
            false,
            false);

    //private static final Logger LOG = Logger.getLogger(SesamSyntaxQueryBuilder.class);


    // Attributes ----------------------------------------------------

    private final SearchConfiguration searchConf;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public SesamSyntaxQueryBuilder(final Context cxt, final SearchConfiguration searchConf) {

        super(cxt, SESAM_SYNTAX_CONFIG);
        this.searchConf = searchConf;
    }

    // AbstractReflectionVisitor implementation ----------------------------------------------

    private boolean insideOr = false;

    /** Avoids writting out fields to terms that
     * do not come from the original query,  are not possible for the user to use.
     *
     * {@inheritDoc}
     * @param clause {@inheritDoc}
     */
    @Override
    protected void visitImpl(LeafClause clause) {

        if(!isEmptyLeaf(clause)){

            String transformedClause = getEscapedTransformedTerm(clause);

            if(null == clause.getField() && transformedClause.contains(":") && !transformedClause.contains("\\:")){

                final String field = transformedClause.substring(0,transformedClause.indexOf(':'));

                if(!searchConf.getFieldFilterMap().containsValue(field)){

                    // query transformation has prepended the term with fields that are meaningless to the user.
                    transformedClause = transformedClause.replace(field + ':', "");
                }
            }

            appendToQueryRepresentation(transformedClause);
        }
    }

    /** Overridden to detect and prevent writing out multiple orGroupOpen and orGroupClose ie ()'s
     * {@inheritDoc}
     * @param clause {@inheritDoc}
     */
    @Override
    protected void visitImpl(final OrClause clause) {

        if (!isEmptyLeaf(clause)) {

            boolean wasInside = insideOr;

            final boolean unary = isEmptyLeaf(clause.getFirstClause()) || isEmptyLeaf(clause.getSecondClause());

            if(!insideOr && getConfig().getOrGrouped() && !unary){
                appendToQueryRepresentation(getConfig().getOrGroupOpen());
            }

            insideOr = true;
            clause.getFirstClause().accept(this);
            if(!isNextLeafInsideNotClause(clause.getSecondClause()) && !unary){
                appendToQueryRepresentation(' ' + getConfig().getDefaultInfix() + ' ');
            }
            clause.getSecondClause().accept(this);
            insideOr = wasInside;

            if(!insideOr && getConfig().getOrGrouped() && !unary){
                appendToQueryRepresentation(getConfig().getOrGroupClose());
            }

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
