// Copyright (2006) Schibsted Søk AS
/*
 * SynonymQueryTransformerTest.java
 *
 * Created on April 5, 2006, 9:32 P
 */

package no.schibstedsok.searchportal.query.transform;


import java.util.Map;

import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.XorClause;
import no.schibstedsok.searchportal.query.parser.AbstractReflectionVisitor;
import no.schibstedsok.searchportal.query.parser.ParseException;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineTestContext;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.searchportal.query.token.TokenPredicate;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 *
 * @author maek
 */
public final class SynonymQueryTransformerTest extends AbstractTransformerTestCase {

    private static final Logger LOG = Logger.getLogger(SynonymQueryTransformerTest.class);
    
    private final SynonymQueryTransformerConfig config = new SynonymQueryTransformerConfig();

    /**
     * 
     * @param testName 
     */
    public SynonymQueryTransformerTest(final String testName) {
        super(testName);
        Logger.getLogger(SynonymQueryTransformer.class).setLevel(org.apache.log4j.Level.TRACE);
        LOG.setLevel(org.apache.log4j.Level.TRACE);
    }

    /**
     * 
     * @throws no.schibstedsok.searchportal.query.parser.ParseException 
     */
    @Test
    public void testOneWordExact() throws ParseException {

        final String queryString = "sch";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);

        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.EXACT_STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);
        final String result = builder.getQueryString();

        LOG.debug("testOneWordExact builder gave " + result);
        if( query.getFirstLeafClause().getPossiblePredicates().contains(TokenPredicate.EXACT_STOCKMARKETTICKERS)){
            assertEquals("(sch schibsted)", result);
        }else{
            assertEquals("sch", result);
        }
    }

    /**
     * 
     * @throws no.schibstedsok.searchportal.query.parser.ParseException 
     */
    @Test
    public void testOneWord() throws ParseException {

        final String queryString = "sch";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);
        final String result = builder.getQueryString();

        LOG.debug("testOneWord builder gave " + result);
        if( query.getFirstLeafClause().getKnownPredicates().contains(TokenPredicate.STOCKMARKETTICKERS)){
            assertEquals("(sch schibsted)", result);
        }else{
            assertEquals("sch", result);
        }
    }

    /**
     * 
     * @throws no.schibstedsok.searchportal.query.parser.ParseException 
     */
    @Test
    public void testTwoWords() throws ParseException {

        final String queryString = "oslo sch schibsted";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);
        final String result = builder.getQueryString();

        LOG.debug("testTwoWords builder gave " + result);
        if( query.getFirstLeafClause().getKnownPredicates().contains(TokenPredicate.STOCKMARKETTICKERS)){
            assertEquals("(oslo oslo areal) (sch schibsted) schibsted", result);
        }else{
            assertEquals("oslo sch schibsted", result);
        }
    }

    /**
     * 
     * @throws no.schibstedsok.searchportal.query.parser.ParseException 
     */
    @Test
    public void testTwoWordsExact() throws ParseException {

        // Not Exact match. Don't do expansion.
        final String queryString = "oslo sch schibsted";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.EXACT_STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);


        final String result = builder.getQueryString();
        LOG.debug("testTwoWordsExact builder gave " + result);
        assertEquals("oslo sch schibsted", result);
    }

// leave this functionality broken until the alternation rotation implementation is fixed.
//    public void testMultiWordOriginalWithOtherTermAtEnd() throws ParseException {
//
//        final String queryString = "schibsted asa oslo";
//        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
//        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);
//
//        final Query query = parseQuery(tef);
//        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(), query,
//                TokenPredicate.COMPANYRANK.name(), tefCxt, tef);
//
//        final QueryBuilder builder = new QueryBuilder(query, trans);
//        final String result = builder.getQueryString();
//
//        LOG.debug("testMultiWordOriginalWithOtherTermAtEnd builder gave " + result);
//        if( query.getFirstLeafClause().getKnownPredicates().contains(TokenPredicate.COMPANYRANK)){
//            assertEquals("(schibsted asa schasa) (oslo oslo areal)", result);
//        }else{
//            assertEquals("schibsted asa oslo", result);
//        }
//    }

    private Map<Clause,String> applyTransformer(
            final SynonymQueryTransformer t,
            final Query query,
            final String predicateName,
            final TokenEvaluationEngineImpl.Context tefCxt,
            final TokenEvaluationEngine tef) {

        t.addPredicateName(predicateName);
        return super.applyTransformer(t,query,tefCxt,tef);
    }


    /**
     * 
     */
    public static final class QueryBuilder extends AbstractReflectionVisitor {
        private final Query query;
        private final Map map;
        private final StringBuilder sb = new StringBuilder();

        /**
         * 
         * @param q 
         * @param m 
         */
        public QueryBuilder(final Query q, final Map m) {
            query = q;
            map = m;
        }

        /**
         * 
         * @return 
         */
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
