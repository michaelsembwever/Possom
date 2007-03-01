package no.schibstedsok.searchportal.query.transform;

import org.w3c.dom.Element;
import org.apache.log4j.Logger;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.Clause;

import java.util.Map;

public class NewsCaseQueryTransformer extends AbstractQueryTransformer {
    private final static Logger log = Logger.getLogger(NewsCaseQueryTransformer.class);
    private static final String QUERY_TYPE = "query-type";
    private String queryType;
    private NewsQueryTransformerDataAccess dataAccess = new NewsQueryTransformerDataAccess();

    public void visitImpl(final LeafClause clause) {
        final String term = getTransformedTerms().get(clause);

        String transformedQuery = dataAccess.getQuery(term, queryType);
        if (transformedQuery != null) {
            log.debug("Transforming query: '" + term + "' to '" + transformedQuery);
            getTransformedTerms().put(clause, transformedQuery);
        } else {
            log.debug("No transformation done.");
        }
    }

    private Map<Clause,String> getTransformedTerms() {
        return getContext().getTransformedTerms();
    }


    @Override
    public QueryTransformer readQueryTransformer(final Element element) {
        queryType = element.getAttribute(QUERY_TYPE);
        return this;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        NewsCaseQueryTransformer ncqt = (NewsCaseQueryTransformer) super.clone();
        ncqt.queryType = queryType;
        ncqt.dataAccess = dataAccess;
        return ncqt;
    }

    public static class NewsQueryTransformerDataAccess {
        public String getQuery(String newsCaseName, String queryType) {
            if (newsCaseName.equals("Nokas-saken")) {
                if (queryType.equals("news")) {
                    return "Nokas-ranet; Arne Sigve Klungland drept";
                } else if (queryType.equals("picture")) {
                    return "Nokas";
                }
            }
            return null;
        }
    }
}
