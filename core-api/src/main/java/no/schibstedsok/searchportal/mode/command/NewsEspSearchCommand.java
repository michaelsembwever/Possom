package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.mode.config.NewsEspCommandConfig;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import org.apache.log4j.Logger;

public class NewsEspSearchCommand extends NavigatableESPFastCommand {
    private static final Logger LOG = Logger.getLogger(NewsEspSearchCommand.class);

    public NewsEspSearchCommand(Context cxt) {
        super(cxt);
    }

    private void addMedium(Clause clause) {
        if (getQuery().getRootClause() == clause) {
            NewsEspCommandConfig config = getSearchConfiguration();
            String medium = (String) datamodel.getJunkYard().getValue(config.getMediumParameter());
            if (!NewsEspCommandConfig.ALL_MEDIUMS.equals(medium) && getTransformedQuery().length() > 0) {
                if (medium == null || medium.length() == 0) {
                    medium = config.getDefaultMedium();
                }
                insertToQueryRepresentation(0, "and(");
                appendToQueryRepresentation(',');
                appendToQueryRepresentation(config.getMediumPrefix());
                appendToQueryRepresentation(':');
                appendToQueryRepresentation(medium);
                appendToQueryRepresentation(')');
                LOG.debug("Added medium");
            }

        }
    }

    protected void visitImpl(final Clause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final LeafClause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OperationClause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndClause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final OrClause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final DefaultOperatorClause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final NotClause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    protected void visitImpl(final AndNotClause clause) {
        LOG.debug("Visited me with: " + clause + ", isRoot=" + (getQuery().getRootClause() == clause));
        super.visitImpl(clause);
        addMedium(clause);
    }

    @Override
    public NewsEspCommandConfig getSearchConfiguration() {
        return (NewsEspCommandConfig) super.getSearchConfiguration();
    }
}
