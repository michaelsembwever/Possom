// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.query.transform;

import no.schibstedsok.newsadmin.service.NewsCaseFacadeInterface;
import no.schibstedsok.searchportal.query.Clause;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Checks if the query should be transformed from a ejb lookup on the queryString. Transformation will replace
 * the whole query.
 * <p/>
 * <b>Note:</b> This queryTransformer ignores all earlier transforms on the query. It uses the raw querystring
 * to transform the query. All transforms to the resulting query should be done after this.
 */
public class NewsCaseQueryTransformer extends AbstractQueryTransformer {
    private final static Logger LOG = Logger.getLogger(NewsCaseQueryTransformer.class);
    private static final String QUERY_TYPE = "query-type";
    private String queryType;
    private NewsQueryTransformerDataAccess dataAccess = new NewsQueryTransformerDataAccess();

    public void visitImpl(final Clause clause) {
        dataAccess.setProperties(getContext().getDataModel().getSite().getSiteConfiguration().getProperties());
        String transformedQuery = dataAccess.getQuery(getContext().getQuery().getQueryString(), queryType);
        if (transformedQuery == null) {
            transformedQuery = '"' + getContext().getQuery().getQueryString().trim() + '"';
        }
        for (Clause keyClause : getContext().getTransformedTerms().keySet()) {
            getContext().getTransformedTerms().put(keyClause, "");
        }
        LOG.debug("New query is: '" + transformedQuery + "'");
        if (transformedQuery.length() > 0) {
            getContext().getTransformedTerms().put(getContext().getQuery().getFirstLeafClause(), transformedQuery);
        }

    }

    @Override
    public QueryTransformer readQueryTransformer(final Element element) {
        queryType = element.getAttribute(QUERY_TYPE);
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final NewsCaseQueryTransformer ncqt = (NewsCaseQueryTransformer) super.clone();
        ncqt.queryType = queryType;
        ncqt.dataAccess = dataAccess;
        return ncqt;
    }

    public static class NewsQueryTransformerDataAccess {
        private Properties properties;
        private static final String NEWSADMIN_JNDINAME = "newsadmin.jndiname";

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

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
