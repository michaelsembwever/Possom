/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryTestContext;
import no.schibstedsok.front.searchportal.query.AndNotClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.Query;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactory;
import no.schibstedsok.front.searchportal.query.token.TokenEvaluatorFactoryImpl;
import no.schibstedsok.front.searchportal.configuration.FileResourcesSiteConfigurationTest;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.NotClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.WordClause;
import no.schibstedsok.front.searchportal.site.Site;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Test the QueryParser and it's generated visitor pattern.
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
                        return FileResourcesSiteConfigurationTest.valueOf(Site.DEFAULT).getProperties();
                    }

                    public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                        return FileResourceLoader.newPropertiesLoader(this, resource, properties);
                    }
                    public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                        return FileResourceLoader.newDocumentLoader(this, resource, builder);
                    }

                    public Site getSite()  {
                        return Site.DEFAULT;
                    }
                });

        final LeafClause magnus = WordClauseImpl.createWordClause("magnus", "firstName", tokenEvaluatorFactory);
        final LeafClause eklund = WordClauseImpl.createWordClause("eklund", null, tokenEvaluatorFactory);
        final LeafClause ss = PhraseClauseImpl.createPhraseClause("\"schibsted sok\"", null, tokenEvaluatorFactory);

        // build right-leaning tree. requirement of current Clause/QueryParser implementation.
        final Clause a = AndClauseImpl.createAndClause(eklund, ss, tokenEvaluatorFactory);
        final Clause andClause = AndClauseImpl.createAndClause(magnus, a, tokenEvaluatorFactory);


        visitor.visit(andClause);

        final String goldenResult = "firstName:magnus AND eklund AND \"schibsted sok\"";
        LOG.info("Visitor built: " + visitor.getParsedQueryString());
        // assert test
        assertNotNull(visitor.getParsedQueryString());
        assertEquals(goldenResult, visitor.getParsedQueryString());
        assertEquals(goldenResult, andClause.getTerm());
    }

    /** test a visitor on a QuerParser built clause heirarchy.
     **/
    public void testAndOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR 123",
                "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR magnus eklund OR 123",
                "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" magnus eklund OR 123");
    }

    public void testAndOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus AND eklund AND oslo \"magnus eklund\" 123",
                "firstname:magnus AND eklund AND oslo \"magnus eklund\" OR magnus eklund 123",
                "firstname:magnus AND eklund AND oslo \"magnus eklund\" magnus eklund 123");
    }

    public void testAndOrNotAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus eklund oslo magnus AND eklund NOT 123",
                "firstname:magnus eklund oslo magnus AND eklund NOT 123",
                "firstname:magnus eklund oslo magnus AND eklund NOT 123");
    }

    public void testOrAgainstQueryParser() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden gausen oldervoll nordlys");
    }

    public void testNotOrAgainstQueryParser() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden NOT gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll nordlys");
    }

    public void testAndNotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen AND Inderøy Marte AND Elden NOT gausen oldervoll nordlys",
                "Hansen AND Inderøy Marte AND Elden NOT gausen oldervoll nordlys",
                "Hansen AND Inderøy Marte AND Elden NOT gausen oldervoll nordlys");
    }

    public void testAndNotOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll AND nordlys");
    }

    public void testAndnotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen oldervoll nordlys");
    }

    public void testAndAndnotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll nordlys");
    }

    public void testAndAndnotNotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll NOT nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll NOT nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll NOT nordlys");
    }

    public void testAndAndnotNotOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy ANDNOT Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy ANDNOT Marte Elden NOT gausen oldervoll AND nordlys");
    }

    public void testPhoneNumberAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "92221689",
                "92221689",
                "92221689");
    }

    public void testPhoneNumberAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "9222 1689",
                "92221689 OR 9222 1689",
                "92221689 9222 1689");
    }

    public void testPhoneNumberAgainstQueryParser3() {
        basicQueryParserWithTestVisitorImpl(
                "+47 9222 1689",
                "+4792221689",
                "+4792221689");
    }

    public void testUnicodeAgainstQueryParser3() {
        basicQueryParserWithTestVisitorImpl(
                "सिद्धार्थ गौतम",
                "सिद्धार्थ गौतम",
                "सिद्धार्थ गौतम");
    }

    private void basicQueryParserWithTestVisitorImpl(
            final String queryInput,
            final String visitorResult,
            final String rootTerm) {


        LOG.info("Starting testBasicQueryParser with input: " + queryInput);

        final TokenEvaluatorFactory tokenEvaluatorFactory
                = new TokenEvaluatorFactoryImpl(new TokenEvaluatorFactoryTestContext(queryInput));

        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
                public TokenEvaluatorFactory getTokenEvaluatorFactory() {
                    return tokenEvaluatorFactory;
                }
            });

        final Query q = parser.getQuery();
        final TestVisitorImpl visitor = new TestVisitorImpl();

        visitor.visit(q.getRootClause());

        LOG.info("Visitor built: " + visitor.getParsedQueryString());
        LOG.info("Root clause's term: " + q.getRootClause().getTerm());
        // assert test
        assertNotNull(visitor.getParsedQueryString());
        assertEquals(visitorResult, visitor.getParsedQueryString());
        assertEquals(rootTerm, q.getRootClause().getTerm());

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

        public String getParsedQueryString() {
            return sb.toString();
        }

        protected void visitImpl(final NotClause clause) {
            sb.append("NOT ");
            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final AndNotClause clause) {
            sb.append("ANDNOT ");
            clause.getFirstClause().accept(this);
        }

        protected void visitImpl(final WordClause clause) {
            if (clause.getField() != null) {
                sb.append(clause.getField());
                sb.append(":");
            }

            sb.append(clause.getTerm());
        }

        protected void visitImpl(final AndClause clause) {
            clause.getFirstClause().accept(this);
            sb.append(" AND ");
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final OrClause clause) {
            clause.getFirstClause().accept(this);
            sb.append(" OR ");
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final DefaultOperatorClause clause) {
            clause.getFirstClause().accept(this);
            sb.append(" ");
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final PhraseClause clause) {
            if (clause.getField() != null) {
                sb.append(clause.getField());
                sb.append(":");
            }

            sb.append(clause.getTerm());
        }

        protected void visitImpl(final Clause clause) {
            // [FIXME] implement me!
        }
    }

}
