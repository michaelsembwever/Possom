/*
 * SynonymQueryTransformerTest.java
 *
 * Created on April 5, 2006, 9:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query.transform;

import com.thoughtworks.xstream.XStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.FileResourcesSearchTabsCreatorTest;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OperationClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.Visitor;
import no.schibstedsok.front.searchportal.query.XorClause;
import no.schibstedsok.front.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.front.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.front.searchportal.query.parser.ParseException;
import no.schibstedsok.front.searchportal.query.parser.QueryParser;
import no.schibstedsok.front.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.site.Site;
import org.apache.log4j.Logger;

/**
 *
 * @author maek
 */
public class SynonymQueryTransformerTest extends TestCase {

    private static final Logger LOG =
            Logger.getLogger(SynonymQueryTransformerTest.class);

    public SynonymQueryTransformerTest(final String testName) {
        super(testName);
    }

//    public void testOneWordExact() throws ParseException {
//        final Query query = parseQuery("sch");
//        final Map trans = applyTransformer(new SynonymQueryTransformer(), query, "EXACT_STOCKMARKETTICKERS");
//        final QueryBuilder builder = new QueryBuilder(query, trans);
//
//        assertEquals("(sch schibsted)", builder.getQueryString());
//    }

//    public void testOneWord() throws ParseException {
//        final Query query = parseQuery("sch");
//        final Map trans = applyTransformer(new SynonymQueryTransformer(), query, "STOCKMARKETTICKERS");
//        final QueryBuilder builder = new QueryBuilder(query, trans);
//
//        assertEquals("(sch schibsted)", builder.getQueryString());
//    }

//    public void testTwoWords() throws ParseException {
//        final Query query = parseQuery("oslo sch schibsted");
//        final Map trans = applyTransformer(new SynonymQueryTransformer(), query, "STOCKMARKETTICKERS");
//        final QueryBuilder builder = new QueryBuilder(query, trans);
//
//        assertEquals("oslo (sch schibsted) schibsted", builder.getQueryString());
//    }

    public void testTwoWordsExact() throws ParseException {
        // Not Exact match. Don't do expansion.
        final Query query = parseQuery("oslo sch schibsted");
        final Map trans = applyTransformer(new SynonymQueryTransformer(), query, "EXACT_STOCKMARKETTICKERS");
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals("oslo sch schibsted", builder.getQueryString());
    }

    public void testMultiWordOriginalWithOtherTermAtEnd() throws ParseException {
        final Query query = parseQuery("schibsted asa oslo");
        final Map trans = applyTransformer(new SynonymQueryTransformer(), query, "COMPANYRANK");
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals("(schibsted asa schasa) oslo", builder.getQueryString());
    }
    
    private Map applyTransformer(final SynonymQueryTransformer t, final Query query, final String predicateName) {
        
        final Map<Clause,String> transformedTerms = new LinkedHashMap<Clause,String>();
        
        final QueryTransformer.Context qtCxt = new QueryTransformer.Context() {
            
            public Map<Clause,String> getTransformedTerms() {
                return transformedTerms;
            }
            public Site getSite() {
                return Site.DEFAULT;
            }
            public Query getQuery() {
                return query;
            }
            public String getTransformedQuery() {
                return query.getQueryString();
            }
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            
            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return FileResourceLoader.newXStreamLoader(this, resource, xstream);
            }
            
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
        };

        t.addPredicateName(predicateName);
        t.setContext(qtCxt);
        
        final Visitor mapInitialisor = new MapInitialisor(transformedTerms);
        mapInitialisor.visit(query.getRootClause());
        t.visit(query.getRootClause());
        return transformedTerms;
    }
    
    private Query parseQuery(final String queryString) throws ParseException {
        
        final TokenEvaluatorFactory tokenEvaluatorFactory  = new TokenEvaluatorFactoryImpl(
                new TokenEvaluatorFactoryImpl.Context() {
            public String getQueryString() {
                return queryString;
            }
            
            public Properties getApplicationProperties() {
                return FileResourcesSearchTabsCreatorTest.valueOf(Site.DEFAULT).getProperties();
            }
            
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            
            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return FileResourceLoader.newXStreamLoader(this, resource, xstream);
            }
            
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            
            public Site getSite()  {
                return Site.DEFAULT;
            }
        });
        
        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
            public TokenEvaluatorFactory getTokenEvaluatorFactory() {
                return tokenEvaluatorFactory;
            }
        });
        
        final Query query = parser.getQuery();
        return query;
    }
    
    public static final class QueryBuilder extends AbstractReflectionVisitor {
        private final Query query;
        private final Map map;
        private final StringBuffer sb = new StringBuffer();
        
        public QueryBuilder(final Query q, final Map m) {
            query = q;
            map = m;
        }
        
        public synchronized String getQueryString() {
            sb.setLength(0);
            visit(query.getRootClause());
            return sb.toString();
        }
        
        public void visitImpl(final LeafClause clause) {
            sb.append(map.get(clause));
        }
        public void visitImpl(final OperationClause clause) {
            clause.getFirstClause().accept(this);
        }
        public void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            sb.append(" AND ");
            clause.getSecondClause().accept(this);
        }
        public void visitImpl(final OrClause clause) {
            clause.getFirstClause().accept(this);
            sb.append(" OR ");
            clause.getSecondClause().accept(this);
        }
        public void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            sb.append(' ');
            clause.getSecondClause().accept(this);
        }
        public void visitImpl(final NotClause clause) {
            final String childsTerm = (String) map.get(clause.getFirstClause());
            if (childsTerm != null && childsTerm.length() > 0) {
                sb.append("NOT ");
                clause.getFirstClause().accept(this);
            }
        }
        public void visitImpl(final AndNotClause clause) {
            final String childsTerm = (String) map.get(clause.getFirstClause());
            if (childsTerm != null && childsTerm.length() > 0) {
                sb.append("ANDNOT ");
                clause.getFirstClause().accept(this);
            }
        }
        public void visitImpl(final XorClause clause) {
            // [TODO] we need to determine which branch in the query-tree we want to use.
            //  Both branches to a XorClause should never be used.
            clause.getFirstClause().accept(this);
            // clause.getSecondClause().accept(this);
        }
    }
    
    private static class MapInitialisor extends AbstractReflectionVisitor {
        
        private final Map map;
        
        public MapInitialisor(final Map m) {
            map = m;
        }
        
        public void visitImpl(final LeafClause clause) {
            final String fullTerm =
                    (clause.getField() == null ? "" : clause.getField() + ": ")
                    + clause.getTerm();
            
            map.put(clause, fullTerm);
        }
        public void visitImpl(final OperationClause clause) {
            clause.getFirstClause().accept(this);
        }
        public void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        public void visitImpl(final OrClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
        public void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            clause.getSecondClause().accept(this);
        }
    }
}
