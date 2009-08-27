/*Copyright (2008-2009) Schibsted ASA
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

import java.util.Collection;
import java.util.HashSet;
import no.sesat.search.mode.config.querybuilder.InfixQueryBuilderConfig;
import no.sesat.search.mode.config.querybuilder.QueryBuilderConfig;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.EmailClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.UnaryClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.UrlClause;

/** The default QueryBuilder.
 *
 * Is not thread safe.
 * It does not use the QueryBuilderConfig.
 * It blanks out valid filters.
 * Largely mimics the Query tree layout replacing OperatorClauses with the RESERVED_WORDS.
 * Does not write infixes when the next occurring leaf is inside a NotClause.
 *
 * @todo grouping doesn't work due to a dependence on the lean of the binary tree.
 *
 * @version $Id$
 */
public class InfixQueryBuilder extends AbstractQueryBuilder{

    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(InfixQueryBuilder.class);


    // Attributes ----------------------------------------------------


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    public InfixQueryBuilder(final Context cxt, final QueryBuilderConfig config) {
        super(cxt, config);
    }

    // Public -------------------------------------------------------

    // protected ----------------------------------------------


    @Override
    protected InfixQueryBuilderConfig getConfig() {
        return (InfixQueryBuilderConfig) super.getConfig();
    }

    @Override
    protected Collection<String> getWordsToEscape() {

        final Collection<String> words = new HashSet<String>(super.getWordsToEscape());

        if(!getConfig().getAndInfix().isEmpty()){  words.add(getConfig().getAndInfix()); }
        if(!getConfig().getNotPrefix().isEmpty()){ words.add(getConfig().getNotPrefix()); }
        if(!getConfig().getOrInfix().isEmpty()){   words.add(getConfig().getOrInfix()); }

        return words;
    }


    protected void visitImpl(final LeafClause clause) {

        if(!isEmptyLeaf(clause)){
            appendToQueryRepresentation(getEscapedTransformedTerm(clause));
        }
    }

    /**
     * Adds quotes around the Email address and hard escape everything.
     * Since there is the @ symbols in email addresses.
     *
     * @param clause The email clause.
     */
    protected void visitImpl(final EmailClause clause) {

        if(!isEmptyLeaf(clause)){
            appendToQueryRepresentation(getContext().escape(getContext().getTransformedTerm(clause).toLowerCase()));
        }
    }

    /**
     * Adds quotes around the URL and hard escape everything.
     * Since there are so many symbols in URLs.
     *
     * @param clause The url clause.
     */
    protected void visitImpl(final UrlClause clause) {

        if(!isEmptyLeaf(clause)){
            appendToQueryRepresentation(getContext().escape(getContext().getTransformedTerm(clause).toLowerCase()));
        }
    }

    protected void visitImpl(final UnaryClause clause) {

        if (!isEmptyLeaf(clause)) {
            clause.getFirstClause().accept(this);
        }
    }

    protected void visitImpl(final AndClause clause) {

        if (!isEmptyLeaf(clause)) {

            final boolean unary = isEmptyLeaf(clause.getFirstClause()) || isEmptyLeaf(clause.getSecondClause());

            if(getConfig().getAndGrouped() && !unary){ appendToQueryRepresentation(getConfig().getAndGroupOpen()); }
            clause.getFirstClause().accept(this);
            if((!getConfig().getSupportsNot() || !isNextLeafInsideNotClause(clause.getSecondClause())) && !unary){
                appendToQueryRepresentation(' ' + getConfig().getAndInfix() + ' ');
            }
            clause.getSecondClause().accept(this);
            if(getConfig().getAndGrouped() && !unary){ appendToQueryRepresentation(getConfig().getAndGroupClose()); }
        }
    }

    protected void visitImpl(final OrClause clause) {

        if (!isEmptyLeaf(clause)) {

            final boolean unary = isEmptyLeaf(clause.getFirstClause()) || isEmptyLeaf(clause.getSecondClause());

            if(getConfig().getOrGrouped() && !unary){ appendToQueryRepresentation(getConfig().getOrGroupOpen()); }
            clause.getFirstClause().accept(this);
            if((!getConfig().getSupportsNot() || !isNextLeafInsideNotClause(clause.getSecondClause())) && !unary){
                appendToQueryRepresentation(' ' + getConfig().getOrInfix() + ' ');
            }
            clause.getSecondClause().accept(this);
            if(getConfig().getOrGrouped() && !unary){ appendToQueryRepresentation(getConfig().getOrGroupClose()); }

        }
    }

    protected void visitImpl(final DefaultOperatorClause clause) {

        if (!isEmptyLeaf(clause)) {

            final boolean unary = isEmptyLeaf(clause.getFirstClause()) || isEmptyLeaf(clause.getSecondClause());

            if(getConfig().getDefaultGrouped() && !unary){ appendToQueryRepresentation(getConfig().getDefaultGroupOpen()); }
            clause.getFirstClause().accept(this);
            if((!getConfig().getSupportsNot() || !isNextLeafInsideNotClause(clause.getSecondClause())) && !unary){
                appendToQueryRepresentation(' ' + getConfig().getDefaultInfix() + ' ');
            }
            clause.getSecondClause().accept(this);
            if(getConfig().getDefaultGrouped() && !unary){ appendToQueryRepresentation(getConfig().getDefaultGroupClose()); }
        }
    }

    protected void visitImpl(final NotClause clause) {

        if (getConfig().getSupportsNot() && !isEmptyLeaf(clause)) {
            appendToQueryRepresentation(' ' + getConfig().getNotPrefix());
            if(getConfig().getNotGrouped()){ appendToQueryRepresentation(getConfig().getNotGroupOpen()); }
            clause.getFirstClause().accept(this);
            if(getConfig().getNotGrouped()){ appendToQueryRepresentation(getConfig().getNotGroupClose()); }
        }
    }


    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
