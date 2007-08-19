/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 * AbstractTransformerTestCase.java
 *
 * Created on 9 April 2006, 13:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.sesat.searchportal.query.transform;

import java.util.LinkedHashMap;
import java.util.Map;
import no.sesat.searchportal.site.SiteTestCase;
import no.sesat.searchportal.query.AndClause;
import no.sesat.searchportal.query.AndNotClause;
import no.sesat.searchportal.query.Clause;
import no.sesat.searchportal.query.DefaultOperatorClause;
import no.sesat.searchportal.query.LeafClause;
import no.sesat.searchportal.query.NotClause;
import no.sesat.searchportal.query.OperationClause;
import no.sesat.searchportal.query.OrClause;
import no.sesat.searchportal.query.Query;
import no.sesat.searchportal.query.Visitor;
import no.sesat.searchportal.query.XorClause;
import no.sesat.searchportal.query.parser.AbstractQueryParserContext;
import no.sesat.searchportal.query.parser.AbstractReflectionVisitor;
import no.sesat.searchportal.query.parser.ParseException;
import no.sesat.searchportal.query.parser.QueryParser;
import no.sesat.searchportal.query.parser.QueryParserImpl;
import no.sesat.searchportal.query.token.TokenEvaluationEngine;
import no.sesat.searchportal.query.token.TokenEvaluationEngineImpl;
import org.apache.log4j.Logger;

/**
 *
 * @author mick
 */
abstract class AbstractTransformerTestCase extends SiteTestCase {

    private static final Logger LOG =
            Logger.getLogger(AbstractTransformerTestCase.class);

    /** Creates a new instance of AbstractTransformerTestCase */
    public AbstractTransformerTestCase(final String testName) {
        super(testName);
    }


    /** TODO comment me. **/
    protected Map<Clause, String> applyTransformer(
            final QueryTransformer t,
            final Query query,
            final TokenEvaluationEngineImpl.Context tefCxt,
            final TokenEvaluationEngine tef) {

        final Map<Clause, String> transformedTerms = new LinkedHashMap<Clause,String>();
        final QueryTransformer.Context qtCxt = new QueryTransformerTestContext(query,transformedTerms,tef);
        t.setContext(qtCxt);

        final Visitor mapInitialisor = new MapInitialisor(transformedTerms);
        mapInitialisor.visit(query.getRootClause());
        t.visit(query.getRootClause());
        return transformedTerms;
    }

    /** TODO comment me. **/
    protected Query parseQuery(final TokenEvaluationEngine tef) throws ParseException {

        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
            public TokenEvaluationEngine getTokenEvaluationEngine() {
                return tef;
            }
        });

        final Query query = parser.getQuery();
        return query;
    }


    protected static final class QueryBuilder extends AbstractReflectionVisitor {
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

    protected static class MapInitialisor extends AbstractReflectionVisitor {

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
