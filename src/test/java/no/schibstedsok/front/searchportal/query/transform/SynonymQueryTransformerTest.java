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
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;

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
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryTestContext;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.site.Site;

import org.apache.log4j.Logger;

/**
 *
 * @author maek
 */
public final class SynonymQueryTransformerTest extends AbstractTransformerTestCase {

    private static final Logger LOG =
            Logger.getLogger(SynonymQueryTransformerTest.class);

    public SynonymQueryTransformerTest(final String testName) {
        super(testName);
        Logger.getLogger(SynonymQueryTransformer.class).setLevel(org.apache.log4j.Level.TRACE);
        LOG.setLevel(org.apache.log4j.Level.TRACE);
    }

    public void testOneWordExact() throws ParseException {

        final String queryString = "sch";
        final TokenEvaluatorFactoryImpl.Context tefCxt = new TokenEvaluatorFactoryTestContext(queryString);
        final TokenEvaluatorFactory tef = new TokenEvaluatorFactoryImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(), query,
                TokenPredicate.EXACT_STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);

        final String result = builder.getQueryString();
        LOG.debug("testOneWordExact builder gave " + result);
        assertEquals("(sch schibsted)", result);
    }

    public void testOneWord() throws ParseException {

        final String queryString = "sch";
        final TokenEvaluatorFactoryImpl.Context tefCxt = new TokenEvaluatorFactoryTestContext(queryString);
        final TokenEvaluatorFactory tef = new TokenEvaluatorFactoryImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(), query,
                TokenPredicate.STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);

        final String result = builder.getQueryString();
        LOG.debug("testOneWord builder gave " + result);
        assertEquals("(sch schibsted)", result);
    }

    public void testTwoWords() throws ParseException {

        final String queryString = "oslo sch schibsted";
        final TokenEvaluatorFactoryImpl.Context tefCxt = new TokenEvaluatorFactoryTestContext(queryString);
        final TokenEvaluatorFactory tef = new TokenEvaluatorFactoryImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(), query,
                TokenPredicate.STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);


        final String result = builder.getQueryString();
        LOG.debug("testTwoWords builder gave " + result);
        assertEquals("(oslo oslo areal) (sch schibsted) schibsted", result);
    }

    public void testTwoWordsExact() throws ParseException {

        // Not Exact match. Don't do expansion.
        final String queryString = "oslo sch schibsted";
        final TokenEvaluatorFactoryImpl.Context tefCxt = new TokenEvaluatorFactoryTestContext(queryString);
        final TokenEvaluatorFactory tef = new TokenEvaluatorFactoryImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(), query,
                TokenPredicate.EXACT_STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);


        final String result = builder.getQueryString();
        LOG.debug("testTwoWordsExact builder gave " + result);
        assertEquals("oslo sch schibsted", result);
    }

//    public void testMultiWordOriginalWithOtherTermAtEnd() throws ParseException {
//
//        final String queryString = "schibsted asa oslo";
//        final TokenEvaluatorFactoryImpl.Context tefCxt = new TokenEvaluatorFactoryTestContext(queryString);
//        final TokenEvaluatorFactory tef = new TokenEvaluatorFactoryImpl(tefCxt);
//
//        final Query query = parseQuery(tef);
//        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(), query,
//                TokenPredicate.COMPANYRANK.name(), tefCxt, tef);
//
//        final QueryBuilder builder = new QueryBuilder(query, trans);
//
//        final String result = builder.getQueryString();
//        LOG.debug("testMultiWordOriginalWithOtherTermAtEnd builder gave " + result);
//        assertEquals("(schibsted asa schasa) (oslo oslo areal)", result);
//    }
    
    private Map<Clause,String> applyTransformer(
            final SynonymQueryTransformer t,
            final Query query,
            final String predicateName,
            final TokenEvaluatorFactoryImpl.Context tefCxt,
            final TokenEvaluatorFactory tef) {
        
        t.addPredicateName(predicateName);
        return super.applyTransformer(t,query,tefCxt,tef);
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
