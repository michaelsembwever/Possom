package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.searchportal.query.Clause;
import org.apache.log4j.Logger;

public class NewsMediumQueryTransformer extends AbstractQueryTransformer {
    private static final Logger LOG = Logger.getLogger(NewsMediumQueryTransformer.class);
    private NewsMediumQueryTransformerConfig config;

    public NewsMediumQueryTransformer(final QueryTransformerConfig config) {
        this.config = (NewsMediumQueryTransformerConfig) config;
    }

    public void visitImpl(final Clause clause) {
        String medium = (String) getContext().getDataModel().getJunkYard().getValue(config.getMediumParameter());
        final String originalQuery = getTransformedTermsQuery();
        if (!NewsMediumQueryTransformerConfig.ALL_MEDIUMS.equals(medium) && originalQuery.length() > 0) {
            if (medium == null || medium.length() == 0) {
                medium = config.getDefaultMedium();
            }
            StringBuilder query = new StringBuilder(originalQuery);
            query.insert(0, "and(");
            query.append(", ").append(config.getMediumPrefix()).append(':').append(medium).append(')');
            for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
                getContext().getTransformedTerms().put(keyClause, "");
            }
            LOG.debug("Transformed query is: '" + query.toString() + "'");
            getContext().getTransformedTerms().put(getContext().getQuery().getFirstLeafClause(), query.toString());
        }
    }

    private String getTransformedTermsQuery() {
        StringBuilder query = new StringBuilder();
        for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
            query.append(getContext().getTransformedTerms().get(keyClause));
        }
        return query.toString();
    }

}
