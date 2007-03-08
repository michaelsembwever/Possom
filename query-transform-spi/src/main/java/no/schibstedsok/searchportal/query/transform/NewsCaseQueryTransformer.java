package no.schibstedsok.searchportal.query.transform;

import org.w3c.dom.Element;
import org.apache.log4j.Logger;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.DoubleOperatorClause;
import no.schibstedsok.searchportal.query.parser.QueryParser;
import no.schibstedsok.searchportal.query.parser.WordClauseImpl;

import java.util.Map;

public class NewsCaseQueryTransformer extends AbstractQueryTransformer {
    private final static Logger log = Logger.getLogger(NewsCaseQueryTransformer.class);
    private static final String QUERY_TYPE = "query-type";
    private String queryType;
    private NewsQueryTransformerDataAccess dataAccess = new NewsQueryTransformerDataAccess();


    protected void visitImpl(final DoubleOperatorClause clause) {
        final StringBuilder sb = new StringBuilder();
        for (String termValue : getTransformedTerms().values()) {
            if (termValue != null) {
                sb.append(termValue).append(' ');
            }
        }
        final String term = sb.toString().trim();
        String transformedQuery = dataAccess.getQuery(term, queryType);
        if (transformedQuery != null) {
            
        }
    }

    public void visitImpl(final LeafClause clause) {
        final String term = getTransformedTerms().get(clause);
        transformTerms(term, clause);
    }

    private void transformTerms(String term, Clause clause) {
        log.debug("Transforming term: '" + term + "', " + clause.getClass().getName());
        String transformedQuery = dataAccess.getQuery(term, queryType);
        if (transformedQuery != null) {
            getContext().getTransformedTerms().put(clause, transformedQuery);
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
            //todo: This implementation is for testing only...
            if (newsCaseName.equals("Nokas-saken")) {
                if (queryType.equals("news")) {
                    return "or(string(\"Nokas-ranet\"),and(string(\"Arne Sigve Klungland\", string(\"drept\")))";
                } else if (queryType.equals("picture")) {
                    return "Nokas";
                }
            } else if (newsCaseName.equals("Valla saken")) {
                if (queryType.equals("news")) {
                    return "or(and(string(\"Yssen\"), string(\"Valla\")),and(string(\"yssen\"), string(\"oppsigelse\")))";
                } else if (queryType.equals("picture") || queryType.equals("blog") || queryType.equals("video")) {
                    return "Yssen Valla";
                } else if (queryType.equals("encylopedia")) {
                    return "Kontroversen rundt Gerd Liv Valla i 2007";
                }
            }
            return null;
        }
    }
}
