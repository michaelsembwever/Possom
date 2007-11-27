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
package no.sesat.search.mode.command;

import no.sesat.search.query.AndClause;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.OrClause;

/**
 * Command producing queries in advanced query syntax for Fast FDS4.
 */
public abstract class AbstractAdvancedFastSearchCommand extends AbstractSimpleFastSearchCommand {
    // Query Language Operators and Terms
    protected static final String QL_AND = " AND ";
    protected static final String QL_OR = " OR ";
    protected static final String QL_ANDNOT = " ANDNOT ";
    protected static final String QL_TRUE = "#";

    /**
     * Creates new advanced commmand.
     *
     * @param cxt        The context.
     * @param parameters The command parameters.
     */
    public AbstractAdvancedFastSearchCommand(final Context cxt) {

        super(cxt);
    }

    // AbstractReflectionVisitor overrides ----------------------------------------------

    /**
     * True if no term has been emitted yet *
     */
    private boolean firstTerm = true;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitImpl(final LeafClause clause) {

        final String transformedTerm = getTransformedTerm(clause);
        if (transformedTerm != null && transformedTerm.length() > 0) {
            super.visitImpl(clause);
            firstTerm = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(QL_AND);
        clause.getSecondClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitImpl(final OrClause clause) {
        appendToQueryRepresentation("(");
        clause.getFirstClause().accept(this);

        appendToQueryRepresentation(QL_OR);

        clause.getSecondClause().accept(this);
        appendToQueryRepresentation(")");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);

        final boolean hasEmptyLeaf = isEmptyLeaf(clause.getFirstClause()) || isEmptyLeaf(clause.getSecondClause());

        if (!(hasEmptyLeaf || clause.getSecondClause() instanceof NotClause)) {
            appendToQueryRepresentation(QL_AND);
        }

        clause.getSecondClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitImpl(final NotClause clause) {
        // This must be extended to handle more cases
        // and not just the start of the query, e.g. first operand of an or operator.
        if (firstTerm) {
            appendToQueryRepresentation(QL_TRUE);
        }
        appendToQueryRepresentation(QL_ANDNOT);
        clause.getFirstClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visitImpl(final AndNotClause clause) {
        appendToQueryRepresentation(QL_ANDNOT);
        clause.getFirstClause().accept(this);
    }

    /**
     * Returns true iff the clause is a leaf clause and if it will not produce any output in the query representation.
     *
     * @param clause The clause to examine.
     *
     * @return true iff leaf is empty.
     */
    @Override
    protected boolean isEmptyLeaf(final Clause clause) {
        if (clause instanceof LeafClause) {
            final LeafClause leafClause = (LeafClause) clause;
            return getFieldFilter(leafClause) != null || getTransformedTerm(clause).equals("");
        } else {
            return false;
        }
    }
}
