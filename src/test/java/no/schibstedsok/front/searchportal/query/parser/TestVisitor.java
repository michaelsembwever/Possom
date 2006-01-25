/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;


import com.thoughtworks.xstream.XStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.analyzer.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.configuration.FileResourcesSearchTabsCreatorTest;
import no.schibstedsok.front.searchportal.configuration.loaders.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loaders.XStreamLoader;
import no.schibstedsok.front.searchportal.site.Site;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Test the QueryParser's visitor pattern.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 **/
public final class TestVisitor extends TestCase {

    private static final Log LOG = LogFactory.getLog(TestVisitor.class);

    /** test a visitor on a  basic clause heirarchy.
     **/
    public void testBasicTestVisitorImpl() {
        final TestVisitorImpl visitor = new TestVisitorImpl();
        final String queryStr = "firstName:magnus eklund \"schibsted sok\"";

        final TokenEvaluatorFactory tokenEvaluatorFactory  = new TokenEvaluatorFactoryImpl(
                new TokenEvaluatorFactoryImpl.Context() {
                    public String getQueryString() {
                        return queryStr;
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

                    public DocumentLoader newDocumentLoader(String resource, DocumentBuilder builder) {
                        return FileResourceLoader.newDocumentLoader(this, resource, builder);
                    }
                    
                    public Site getSite()  {
                        return Site.DEFAULT;
                    }

                });

        final LeafClause magnus = WordClause.createWordClause("magnus", "firstName", tokenEvaluatorFactory);
        final LeafClause eklund = WordClause.createWordClause("eklund", null, tokenEvaluatorFactory);
        final LeafClause ss = PhraseClause.createPhraseClause("\"schibsted sok\"", null, tokenEvaluatorFactory);

        // build right-leaning tree. requirement of current Clause/QueryParser implementation.
        final Clause a = AndClause.createAndClause(eklund, ss, tokenEvaluatorFactory);
        final Clause andClause = AndClause.createAndClause(magnus, a, tokenEvaluatorFactory);


        visitor.visit(andClause);

        final String goldenResult = "firstName:magnus AND eklund AND \"schibsted sok\"";
        LOG.info("Visitor built: " + visitor.getQueryAsString());
        // assert test
        assertNotNull(visitor.getQueryAsString());
        assertEquals(goldenResult, visitor.getQueryAsString());
        assertEquals(goldenResult, andClause.getTerm());
    }

    /** test a visitor on a QuerParser built clause heirarchy.
     **/
    public void testBasicQueryParserWithTestVisitorImpl() {

        final String queryInput = "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR 123";
        LOG.info("Starting testBasicQueryParser with input: " + queryInput);

        final TokenEvaluatorFactory tokenEvaluatorFactory  = new TokenEvaluatorFactoryImpl(
                new TokenEvaluatorFactoryImpl.Context() {
                    public String getQueryString() {
                        return queryInput;
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

                    public DocumentLoader newDocumentLoader(String resource, DocumentBuilder builder) {
                        return FileResourceLoader.newDocumentLoader(this, resource, builder);
                    }
                    
                    public Site getSite()  {
                        return Site.DEFAULT;
                    }

                });

        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
                public String getQueryString() {
                    return queryInput;
                }
                public TokenEvaluatorFactory getTokenEvaluatorFactory() {
                    return tokenEvaluatorFactory;
                }
            });

        try  {
            final Query q = parser.getQuery();
            final TestVisitorImpl visitor = new TestVisitorImpl();

            visitor.visit(q.getRootClause());

            final String goldenResult = "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR 123";
            LOG.info("Visitor built: " + visitor.getQueryAsString());
            // assert test
            assertNotNull(visitor.getQueryAsString());
            assertEquals(goldenResult, visitor.getQueryAsString());
            assertEquals(goldenResult, q.getRootClause().getTerm());

        }  catch (ParseException ex) {
            LOG.error(ex);
            fail(ex.getLocalizedMessage());
        }

    }

    /** Mickey Mouse visitor implementation to test the basics...
     * The clause interface already provides a getter to the term, which for the root clause should match what this
     * visitor produces. But the getter is only for backward-compatibilty to RegExpTokenEvaluators...
     */
    private static class TestVisitorImpl extends AbstractReflectionVisitor {

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

            //sb.append("\"");
            sb.append(clause.getTerm());
            //sb.append("\"");
        }

        public void visitImpl(final Clause clause) {
            // [FIXME] implement me!
        }
    }

}
