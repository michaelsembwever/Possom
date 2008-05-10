/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
/*
 * SynonymQueryTransformerTest.java
 *
 * Created on April 5, 2006, 9:32 P
 */

package no.sesat.search.query.transform;


import java.util.Map;

import no.sesat.search.query.AndClause;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.OperationClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.XorClause;
import no.sesat.search.query.parser.AbstractReflectionVisitor;
import no.sesat.search.query.parser.ParseException;
import no.sesat.search.query.token.TokenEvaluationEngineTestContext;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenEvaluationEngineImpl;
import no.sesat.search.query.token.TokenPredicate;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 *
 *
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
     * @throws no.sesat.search.query.parser.ParseException
     */
    @Test
    public void testOneWordExact() throws ParseException {

        final String queryString = "sch";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);

        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.Categories.STOCKMARKETTICKERS.exactPeer().name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);
        final String result = builder.getQueryString();

        LOG.debug("testOneWordExact builder gave " + result);
        if( query.getFirstLeafClause().getPossiblePredicates().contains(TokenPredicate.Categories.STOCKMARKETTICKERS.exactPeer())){
            assertEquals("(sch schibsted)", result);
        }else{
            assertEquals("sch", result);
        }
    }

    /**
     *
     * @throws no.sesat.search.query.parser.ParseException
     */
    @Test
    public void testOneWord() throws ParseException {

        final String queryString = "sch";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.Categories.STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);
        final String result = builder.getQueryString();

        LOG.debug("testOneWord builder gave " + result);
        if( query.getFirstLeafClause().getKnownPredicates().contains(TokenPredicate.Categories.STOCKMARKETTICKERS)){
            assertEquals("(sch schibsted)", result);
        }else{
            assertEquals("sch", result);
        }
    }

    /**
     *
     * @throws no.sesat.search.query.parser.ParseException
     */
    @Test
    public void testTwoWords() throws ParseException {

        final String queryString = "oslo sch schibsted";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.Categories.STOCKMARKETTICKERS.name(), tefCxt, tef);

        final QueryBuilder builder = new QueryBuilder(query, trans);
        final String result = builder.getQueryString();

        LOG.debug("testTwoWords builder gave " + result);
        if( query.getFirstLeafClause().getKnownPredicates().contains(TokenPredicate.Categories.STOCKMARKETTICKERS)){
            assertEquals("(oslo oslo areal) (sch schibsted) schibsted", result);
        }else{
            assertEquals("oslo sch schibsted", result);
        }
    }

    /**
     *
     * @throws no.sesat.search.query.parser.ParseException
     */
    @Test
    public void testTwoWordsExact() throws ParseException {

        // Not Exact match. Don't do expansion.
        final String queryString = "oslo sch schibsted";
        final TokenEvaluationEngineImpl.Context tefCxt = new TokenEvaluationEngineTestContext(queryString);
        final TokenEvaluationEngine tef = new TokenEvaluationEngineImpl(tefCxt);

        final Query query = parseQuery(tef);
        final Map<Clause,String> trans = applyTransformer(new SynonymQueryTransformer(config), query,
                TokenPredicate.Categories.STOCKMARKETTICKERS.exactPeer().name(), tefCxt, tef);

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
