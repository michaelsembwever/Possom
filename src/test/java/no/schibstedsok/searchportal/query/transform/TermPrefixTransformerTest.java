// Copyright (2006) Schibsted Søk AS
/*
 * TermPrefixTransformerTest.java
 * JUnit based test
 *
 * Created on March 2, 2006, 9:48 AM
 */

package no.schibstedsok.searchportal.query.transform;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import no.schibstedsok.searchportal.TestCase;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;

import no.schibstedsok.searchportal.mode.config.FileResourcesSiteConfigurationTest;
import no.schibstedsok.searchportal.util.config.DocumentLoader;
import no.schibstedsok.searchportal.util.config.FileResourceLoader;
import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.Visitor;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractQueryParserContext;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.parser.ParseException;
import no.schibstedsok.searchportal.query.parser.QueryParser;
import no.schibstedsok.searchportal.query.parser.QueryParserImpl;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineTestContext;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.searchportal.site.Site;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 *
 * @author magnuse
 */
public class TermPrefixTransformerTest extends AbstractTransformerTestCase {

    private static final String PREFIX_WORD = "wordprefix";
    private static final String PREFIX_INTEGER = "integerprefix";

    private static final String QUERY_WORD = "singleword";
    private static final String QUERY_WORD_2 = "singleword2";
    private static final String QUERY_PHONE_NUMBER = "97403306";
    private static final String QUERY_PHONE_NUMBER_SPACES = "97 40 33 06";
    private static final String QUERY_INTEGER = "524287";
    private static final String QUERY_ORG_NR = "880990152";

    private static final Logger LOG =
            Logger.getLogger(TermPrefixTransformerTest.class);

    public TermPrefixTransformerTest(final String testName) {
        super(testName);
    }

    @Test
    public void testTwoSameWordsQuery() throws ParseException {

        final String queryString = QUERY_WORD + " " + QUERY_WORD;
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map trans = applyTransformer(new TermPrefixTransformer(), query, tefCxt, tef);
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals(PREFIX_WORD + ":" + QUERY_WORD + " " +
                PREFIX_WORD + ":" + QUERY_WORD, builder.getQueryString());
    }

    @Test
    public void testTwoWordQuery() throws ParseException {

        final String queryString = QUERY_WORD + " " + QUERY_WORD_2;

        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map trans = applyTransformer(new TermPrefixTransformer(), query, tefCxt, tef);
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals(PREFIX_WORD + ":" + QUERY_WORD + " " +
                PREFIX_WORD + ":" + QUERY_WORD_2,
                builder.getQueryString());
    }

    @Test
    public void testPhoneNumber() throws ParseException {

        final String queryString = QUERY_PHONE_NUMBER;
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map trans = applyTransformer(new TermPrefixTransformer(), query, tefCxt, tef);
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals(PREFIX_INTEGER + ":" + QUERY_PHONE_NUMBER,
                builder.getQueryString());
    }

    @Test
    public void testPhoneNumberSpaces() throws ParseException {

        final String queryString = QUERY_PHONE_NUMBER_SPACES;
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map trans = applyTransformer(new TermPrefixTransformer(), query, tefCxt, tef);
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals(PREFIX_INTEGER + ":" + QUERY_PHONE_NUMBER,
                builder.getQueryString());
    }
    
    @Test
    public void testOrgNr() throws ParseException {

        final String queryString = QUERY_ORG_NR;
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map trans = applyTransformer(new TermPrefixTransformer(), query, tefCxt, tef);
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals(PREFIX_INTEGER + ":" + QUERY_ORG_NR,
                builder.getQueryString());
    }

    @Test
    public void testInteger() throws ParseException {

        final String queryString = QUERY_INTEGER;
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map trans = applyTransformer(new TermPrefixTransformer(), query, tefCxt, tef);
        final QueryBuilder builder = new QueryBuilder(query, trans);

        assertEquals(PREFIX_INTEGER + ":" + QUERY_INTEGER,
                builder.getQueryString());
    }

    protected Map<Clause, String> applyTransformer(
            final TermPrefixTransformer t,
            final Query query,
            final TokenEvaluationEngineImpl.Context tefCxt,
            final TokenEvaluationEngine tef) {

        t.setPrefix(PREFIX_WORD);
        t.setNumberPrefix(PREFIX_INTEGER);
        return super.applyTransformer(t, query, tefCxt, tef);
    }


    public static final class QueryBuilder extends AbstractReflectionVisitor {
        private final Query query;
        private final Map map;
        private final StringBuilder sb = new StringBuilder();

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

            LOG.debug("Construct " + map.get(clause));
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
