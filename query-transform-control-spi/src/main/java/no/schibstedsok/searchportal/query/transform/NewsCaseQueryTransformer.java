// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.newsadmin.service.NewsCaseFacadeInterface;
import no.schibstedsok.searchportal.query.Clause;
import org.apache.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Checks if the query should be transformed from a ejb lookup on the queryParameter. Transformation will replace
 * the whole query.
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. All transforms to the resulting
 * query should be done after this.
 */
public final class NewsCaseQueryTransformer extends AbstractQueryTransformer {
    private final static Logger LOG = Logger.getLogger(NewsCaseQueryTransformer.class);

    private NewsQueryTransformerDataAccess dataAccess = new NewsQueryTransformerDataAccess();

    private final NewsCaseQueryTransformerConfig config;

    /**
     * @param config
     */
    public NewsCaseQueryTransformer(final QueryTransformerConfig config) {
        this.config = (NewsCaseQueryTransformerConfig) config;
    }


    /**
     * @param clause
     */
    public void visitImpl(final Clause clause) {
        dataAccess.setProperties(getContext().getDataModel().getSite().getSiteConfiguration().getProperties());
        String queryString = (String) getContext().getDataModel().getJunkYard().getValue(config.getQueryParameter());
        LOG.debug("Original query is: '" + queryString + "'");
        String transformedQuery = dataAccess.getQuery(queryString, config.getQueryType());
        if (transformedQuery == null) {
            transformedQuery = defaultTransform(queryString);
        }
        for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
            getContext().getTransformedTerms().put(keyClause, "");
        }
        LOG.debug("New query is: '" + transformedQuery + "'");
        if (transformedQuery.length() > 0) {
            getContext().getDataModel().getJunkYard().setValue(config.getQueryType(), transformedQuery);
            getContext().getTransformedTerms().put(getContext().getQuery().getFirstLeafClause(), transformedQuery);
        }

    }

    private String defaultTransform(String queryString) {
        if (config.getTypeConversions() != null) {
            String type = (String) getContext().getDataModel().getJunkYard().getValue(config.getTypeParameter());
            if (type != null) {
                String[] conversion = config.getTypeConversions().get(type);
                if (conversion != null) {
                    return conversion[0] + queryString + conversion[1];
                }
            }
        }
        return '"' + queryString + '"';
    }

    /**
     *
     */
    public static class NewsQueryTransformerDataAccess {
        private Properties properties;
        private static final String NEWSADMIN_JNDINAME = "newsadmin.jndiname";

        /**
         * @param properties
         */
        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        /**
         * @return
         */
        public NewsCaseFacadeInterface lookupDataService() {
            InitialContext ic = null;
            try {
                String serviceJndi = properties.getProperty(NEWSADMIN_JNDINAME);
                ic = new InitialContext(properties);
                return (NewsCaseFacadeInterface) ic.lookup(serviceJndi);
            } catch (NamingException e) {
                LOG.error("Could not lookup remote EJB.", e);
            } finally {
                if (ic != null) {
                    try {
                        ic.close();
                    } catch (NamingException e) {
                        // Ignoring
                    }
                }
            }
            return null;
        }

        /**
         * @param newsCaseName
         * @param queryType
         * @return
         */
        public String getQuery(String newsCaseName, String queryType) {
            try {
                LOG.debug("Looking up query for: " + newsCaseName);
                final NewsCaseFacadeInterface newsCaseFacade = lookupDataService();
                if (newsCaseFacade != null) {
                    String newsQuery = newsCaseFacade.searchForQuery(newsCaseName, queryType);
                    if (newsQuery != null) {
                        return newsQuery;
                    }
                }
            } catch (RuntimeException e) {
                LOG.error("Could not lookup query for: " + newsCaseName, e);
            }
            return null;
        }

    }
}
