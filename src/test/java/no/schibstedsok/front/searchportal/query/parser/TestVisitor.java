/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.configuration.XMLSearchTabsCreator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class TestVisitor extends TestCase{

    private static final Log LOG = LogFactory.getLog(TestVisitor.class);

    public void testBasicTestVisitorImpl(){
        final TestVisitorImpl visitor = new TestVisitorImpl();
        final String queryStr = "firstName:magnus eklund \"schibsted sok\"";
        
        final TokenEvaluatorFactory tokenEvaluatorFactory  = new TokenEvaluatorFactoryImpl(
                new TokenEvaluatorFactoryImpl.Context(){
                    public String getQueryString() {
                        return queryStr;
                    }

                    public Properties getApplicationProperties() {
                        return XMLSearchTabsCreator.getInstance().getProperties();
                    }

                });

        final Clause magnus = WordClause.createWordClause("magnus", "firstName",tokenEvaluatorFactory);
        final Clause eklund = WordClause.createWordClause("eklund", null, tokenEvaluatorFactory);
        final Clause ss = PhraseClause.createPhraseClause("schibsted sok", null, tokenEvaluatorFactory);
        final Clause andClause = new AndClause(magnus, eklund);
        final Clause a = new AndClause(andClause, ss);
        
        visitor.visit(a);
        
        final String goldenResult = "firstName:magnus AND eklund AND \"schibsted sok\"";
        LOG.info("Visitor built: "+visitor.getQueryAsString());
        // assert test
        assertNotNull(visitor.getQueryAsString());
        assertEquals(goldenResult,visitor.getQueryAsString());
    }
    
    public void testBasicQueryParserWithTestVisitorImpl(){

        final String queryInput = "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR 123";
        LOG.info("Starting testBasicQueryParser with input: " + queryInput);
        
        final TokenEvaluatorFactory tokenEvaluatorFactory  = new TokenEvaluatorFactoryImpl(
                new TokenEvaluatorFactoryImpl.Context(){
                    public String getQueryString() {
                        return queryInput;
                    }

                    public Properties getApplicationProperties() {
                        return XMLSearchTabsCreator.getInstance().getProperties();
                    }

                });
                
        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext(){
                public String getQueryString(){
                    return queryInput;
                }
                public TokenEvaluatorFactory getTokenEvaluatorFactory(){
                    return tokenEvaluatorFactory;
                }
            });
        
        try{
            final Query q = parser.getQuery();
            final TestVisitorImpl visitor = new TestVisitorImpl();
            
            visitor.visit(q.getRootClause());
            
            final String goldenResult = "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR 123";
            LOG.info("Visitor built: "+visitor.getQueryAsString());
            // assert test
            assertNotNull(visitor.getQueryAsString());
            assertEquals(goldenResult,visitor.getQueryAsString());

        }catch(ParseException ex){
            LOG.error(ex);
            fail(ex.getLocalizedMessage());
        }

    }

    /** Mickey Mouse visitor implementation to test the basics... */
    private static class TestVisitorImpl extends AbstractReflectionVisitor{

        private final StringBuffer sb;

        public TestVisitorImpl() {
            sb = new StringBuffer();
        }

        public String getQueryAsString() {
            return sb.toString();
        }

        public void visitImpl(final NotClause clause) {
            sb.append("-");
            clause.getClause().accept(this);
        }

        public void visitImpl(final WordClause clause) {
            if (clause.getField() != null) {
                sb.append(clause.getField());
                sb.append(":");
            }

            sb.append(clause.getTerm());
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

        public void visitImpl(final PhraseClause clause) {
            if (clause.getField() != null) {
                sb.append(clause.getField());
                sb.append(":");
            }

            sb.append("\"");
            sb.append(clause.getTerm());
            sb.append("\"");
        }

        public void visitImpl(final Clause clause) {
            // [FIXME] implement me!
        }
    }

}
