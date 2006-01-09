/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.io.StringReader;
import java.util.Iterator;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class TestVisitor extends TestCase{

    private static final Log LOG = LogFactory.getLog(TestVisitor.class);

    public void testBasicVisitorImpl(){
        final QueryVisitor visitor = new VisitorImpl();

        final Clause magnus = new WordClause("magnus", "firstName");
        final Clause eklund = new WordClause("eklund");
        final Clause ss = new PhraseClause("schibsted sok");
        final Clause andClause = new AndClause(magnus, eklund);
        final Clause a = new AndClause(andClause, ss);
        
        visitor.visit(a);
        
        final String goldenResult = "firstName:magnus eklund \"schibsted sok\"";
        LOG.info("Visitor built: "+visitor.getQueryAsString());
        // assert test
        assertNotNull(visitor.getQueryAsString());
        assertEquals(goldenResult,visitor.getQueryAsString());
    }
    
    public void testBasicQueryParser(){

        final String queryInput = "magnus AND eklund AND oslo OR \"magnus eklund\" 123";
        LOG.info("Starting testBasicQueryParser with input: " + queryInput);
        final QueryParser p = new QueryParser(new StringReader(queryInput));
        try{
            final Clause c = p.parse();
            final QueryVisitor visitor = new VisitorImpl();
            
            visitor.visit(c);
            
            final String goldenResult = "magnus eklund oslo OR \"magnus eklund\" 123";
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
    private static class VisitorImpl extends AbstractReflectionVisitor implements QueryVisitor{

        private final StringBuffer sb;

        public VisitorImpl() {
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
            sb.append(" ");
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
            sb.append(clause.getPhrase());
            sb.append("\"");
        }

        public void visitImpl(final AllClause clausePart) {

            for (Iterator iter = clausePart.getClauses().iterator(); iter.hasNext();) {
                final Clause clause = (Clause) iter.next();
                clause.accept(this);
                if (iter.hasNext()) {
                    sb.append(" ");
                }
            }
        }

        public void visitImpl(final Clause clause) {
            // [FIXME] implement me!
        }
    }

}
