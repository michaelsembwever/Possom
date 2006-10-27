package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;

import java.util.Map;

/**
 * Command producing queries in advanced query syntax for Fast FDS4.
 */
public abstract class AbstractAdvancedFastSearchCommand extends AbstractSimpleFastSearchCommand {
    // Query Language Operators and Terms
    private static final String QL_AND = " AND ";
    private static final String QL_OR = " OR ";
    private static final String QL_ANDNOT = " ANDNOT ";
    private static final String QL_TRUE = "#";

    /**
     * Creates new advanced commmand.
     *
     * @param cxt        The context.
     * @param parameters The command parameters.
     */
    public AbstractAdvancedFastSearchCommand(
            final Context cxt,
            final Map parameters) {

        super(cxt, parameters);
    }

    // AbstractReflectionVisitor overrides ----------------------------------------------

    /**
     * True if no term has been emitted yet *
     */
    private boolean firstTerm = true;

    /**
     * {@inheritDoc}
     */
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
    protected void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        appendToQueryRepresentation(QL_AND);
        clause.getSecondClause().accept(this);
    }

    /**
     * {@inheritDoc}
     */
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
    private boolean isEmptyLeaf(final Clause clause) {
        if (clause instanceof LeafClause) {
            final LeafClause leaf = (LeafClause) clause;
            return context.getSearchConfiguration().getFieldFilters().keySet().contains(leaf.getField());
        } else {
            return false;
        }
    }
}
