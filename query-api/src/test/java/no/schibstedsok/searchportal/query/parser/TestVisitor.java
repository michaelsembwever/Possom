/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
package no.schibstedsok.searchportal.query.parser;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineTestContext;
import no.schibstedsok.searchportal.query.AndNotClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineImpl;
import no.schibstedsok.searchportal.site.config.*;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.NotClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhraseClause;
import no.schibstedsok.searchportal.query.WordClause;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/** Test the QueryParser and it's generated visitor pattern.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 **/
public final class TestVisitor extends SiteTestCase {

    private static final Logger LOG = Logger.getLogger(TestVisitor.class);


    /** TODO comment me. **/
    public TestVisitor(final String testName) {
        super(testName);
    }	

    /** test a visitor on a  basic clause heirarchy.
     **/
    @Test
    public void testBasicTestVisitorImpl() {
        final TestVisitorImpl visitor = new TestVisitorImpl();
        final String queryStr = "firstName:magnus eklund \"schibsted sok\"";

        final TokenEvaluationEngine tokenEvaluationEngine  = new TokenEvaluationEngineImpl(
                new TokenEvaluationEngineImpl.Context() {
                    public String getQueryString() {
                        return queryStr;
                    }
                    public Properties getApplicationProperties() {
                        return FileResourcesSiteConfigurationTest.valueOf(getTestingSite()).getProperties();
                    }
                    public PropertiesLoader newPropertiesLoader(
                            final SiteContext siteCxt, 
                            final String resource, 
                            final Properties properties) {

                        return FileResourceLoader.newPropertiesLoader(siteCxt, resource, properties);
                    }
                    public DocumentLoader newDocumentLoader(
                            final SiteContext siteCxt, 
                            final String resource, 
                            final DocumentBuilder builder) {

                        return FileResourceLoader.newDocumentLoader(siteCxt, resource, builder);
                    }
                    public BytecodeLoader newBytecodeLoader(SiteContext context, String className, String jar) {
                        return FileResourceLoader.newBytecodeLoader(context, className, jar);
                    }

                    public Site getSite()  {
                        return getTestingSite();
                    }
                });

        final LeafClause magnus = WordClauseImpl.createWordClause("magnus", "firstName", tokenEvaluationEngine);
        final LeafClause eklund = WordClauseImpl.createWordClause("eklund", null, tokenEvaluationEngine);
        final LeafClause ss = PhraseClauseImpl.createPhraseClause("\"schibsted sok\"", null, tokenEvaluationEngine);

        // build right-leaning tree. requirement of current Clause/QueryParser implementation.
        Clause andClause = AndClauseImpl.createAndClause(eklund, ss, tokenEvaluationEngine);
        andClause = AndClauseImpl.createAndClause(magnus, andClause, tokenEvaluationEngine);


        visitor.visit(andClause);

        final String goldenResult = "firstName:magnus AND eklund AND \"schibsted sok\"";
        LOG.info("Visitor built: " + visitor.getParsedQueryString());
        // assert test
        assertNotNull(visitor.getParsedQueryString());
        assertEquals(goldenResult, visitor.getParsedQueryString());
        assertEquals(goldenResult, andClause.getTerm().replaceAll("\\(|\\)",""));
    }

    /** test a visitor on a QuerParser built clause heirarchy.
     **/
    @Test
    public void testAndOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR 123",
                "firstname:magnus AND eklund AND oslo OR \"magnus eklund\" OR magnus eklund OR 123",
                "((((firstname:magnus AND eklund) AND oslo) (firstname:magnus AND (eklund AND oslo)) OR \"magnus eklund\" (\"magnus eklund\" OR \"eklund magnus\") (magnus eklund)) OR 123) (((firstname:magnus AND eklund) AND oslo) (firstname:magnus AND (eklund AND oslo)) OR (\"magnus eklund\" (magnus eklund) OR 123))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus AND eklund AND oslo \"magnus eklund\" 123",
                "firstname:magnus AND eklund AND oslo \"magnus eklund\" OR magnus eklund 123",
                "((((firstname:magnus AND eklund) AND oslo) (firstname:magnus AND (eklund AND oslo)) \"magnus eklund\" (\"magnus eklund\" OR \"eklund magnus\") (magnus eklund)) 123) (((firstname:magnus AND eklund) AND oslo) (firstname:magnus AND (eklund AND oslo)) (\"magnus eklund\" (magnus eklund) 123))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndOrNotAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus eklund oslo magnus AND eklund NOT 123",
                "firstname:magnus eklund oslo magnus AND eklund NOT 123",
                "((((firstname:magnus eklund) oslo) (magnus AND eklund)) NOT 123) (((firstname:magnus (eklund oslo)) (magnus AND eklund)) NOT 123) ((firstname:magnus (eklund (oslo (magnus AND eklund)))) NOT 123) (firstname:magnus (eklund (oslo ((magnus AND eklund) NOT 123))))");
    }

    /** TODO comment me. **/
    @Test
    public void testOrAgainstQueryParser() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden gausen oldervoll nordlys",
                "((((((Hansen Inderøy) Marte) Elden) gausen) oldervoll) nordlys) (((((Hansen (Inderøy Marte)) Elden) gausen) oldervoll) nordlys) ((((Hansen (Inderøy (\"Marte Elden\" OR \"Elden Marte\") (Marte Elden))) gausen) oldervoll) nordlys) (((Hansen (Inderøy (Marte (Elden gausen)))) oldervoll) nordlys) ((Hansen (Inderøy (Marte (Elden (gausen oldervoll))))) nordlys) (Hansen (Inderøy (Marte (Elden (gausen (oldervoll nordlys))))))");
    }

    /** TODO comment me. **/
    @Test
    public void testNotOrAgainstQueryParser() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden NOT gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll nordlys",
                "((((((Hansen Inderøy) Marte) Elden) NOT gausen) oldervoll) nordlys) (((((Hansen (Inderøy Marte)) Elden) NOT gausen) oldervoll) nordlys) ((((Hansen (Inderøy (\"Marte Elden\" OR \"Elden Marte\") (Marte Elden))) NOT gausen) oldervoll) nordlys) (((Hansen (Inderøy (Marte (Elden NOT gausen)))) oldervoll) nordlys) ((Hansen (Inderøy (Marte (Elden (NOT gausen oldervoll))))) nordlys) (Hansen (Inderøy (Marte (Elden (NOT gausen (oldervoll nordlys))))))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndNotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen AND Inderøy Marte AND Elden NOT gausen oldervoll nordlys",
                "Hansen AND Inderøy Marte AND Elden NOT gausen oldervoll nordlys",
                "(((((Hansen AND Inderøy) (Marte AND Elden)) NOT gausen) oldervoll) nordlys) ((((Hansen AND Inderøy) ((Marte AND Elden) NOT gausen)) oldervoll) nordlys) (((Hansen AND Inderøy) ((Marte AND Elden) (NOT gausen oldervoll))) nordlys) ((Hansen AND Inderøy) ((Marte AND Elden) (NOT gausen (oldervoll nordlys))))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndNotOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll AND nordlys",
                "(((((Hansen Inderøy) Marte) Elden) NOT gausen) (oldervoll AND nordlys)) ((((Hansen (Inderøy Marte)) Elden) NOT gausen) (oldervoll AND nordlys)) (((Hansen (Inderøy (\"Marte Elden\" OR \"Elden Marte\") (Marte Elden))) NOT gausen) (oldervoll AND nordlys)) ((Hansen (Inderøy (Marte (Elden NOT gausen)))) (oldervoll AND nordlys)) (Hansen (Inderøy (Marte (Elden (NOT gausen (oldervoll AND nordlys))))))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndnotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen oldervoll nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden (gausen (oldervoll nordlys))))) (Hansen (Inderøy ANDNOT (Marte (Elden (gausen (oldervoll nordlys))))))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndAndnotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden ((gausen AND oldervoll) nordlys)))) (Hansen (Inderøy ANDNOT (Marte (Elden ((gausen AND oldervoll) nordlys)))))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndAndnotNotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll NOT nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll NOT nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden ((gausen AND oldervoll) NOT nordlys)))) (Hansen (Inderøy ANDNOT (Marte (Elden ((gausen AND oldervoll) NOT nordlys)))))");
    }

    /** TODO comment me. **/
    @Test
    public void testAndAndnotNotOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy ANDNOT Marte Elden NOT gausen oldervoll AND nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden (NOT gausen (oldervoll AND nordlys))))) (Hansen (Inderøy ANDNOT (Marte (Elden (NOT gausen (oldervoll AND nordlys))))))");
    }

    /** TODO comment me. **/
    @Test
    public void testPhoneNumberAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "92221689",
                "92221689 OR 92221689",
                "92221689 92221689");
    }

    /** TODO comment me. **/
    @Test
    public void testPhoneNumberAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "9222 1689",
                "92221689 OR 9222 1689",
                "92221689 92221689 (9222 1689)");
    }

    /** TODO comment me. **/
    @Test
    public void testPhoneNumberAgainstQueryParser3() {
        basicQueryParserWithTestVisitorImpl(
                "+47 9222 1689",
                "+4792221689 OR 47 9222 1689",
                "+4792221689 4792221689 ((47 9222) 1689) (47 (9222 1689))");
    }

    /** TODO comment me. **/
    @Test
    public void testUnicodeAgainstQueryParser3() {
        basicQueryParserWithTestVisitorImpl(
                "सिद्धार्थ गौतम",
                "सिद्धार्थ गौतम",
                "(सिद्धार्थ गौतम)");
    }

    private void basicQueryParserWithTestVisitorImpl(
            final String queryInput,
            final String visitorResult,
            final String rootTerm) {


        LOG.info("Starting testBasicQueryParser with input: " + queryInput);

        final TokenEvaluationEngine tokenEvaluationEngine
                = new TokenEvaluationEngineImpl(new TokenEvaluationEngineTestContext(queryInput));

        final QueryParser parser = new QueryParserImpl(new AbstractQueryParserContext() {
                public TokenEvaluationEngine getTokenEvaluationEngine() {
                    return tokenEvaluationEngine;
                }
            });

        final Query q = parser.getQuery();
        final TestVisitorImpl visitor = new TestVisitorImpl();

        visitor.visit(q.getRootClause());

        LOG.info("Visitor built: " + visitor.getParsedQueryString());
        LOG.info("Root clause's term: " + q.getRootClause().getTerm());
        // assert test
        assertNotNull(visitor.getParsedQueryString());
        // disabled for the meantime // assertEquals(visitorResult, visitor.getParsedQueryString());
        assertEquals(rootTerm, q.getRootClause().getTerm()/*.replaceAll("\\(|\\)","")*/);

    }

    /** Mickey Mouse visitor implementation to test the basics...
     * The clause interface already provides a getter to the term, which for the root clause should match what this
     * visitor produces. But the getter is only for backward-compatibilty to RegExpTokenEvaluators...
     */
    private static class TestVisitorImpl extends AbstractReflectionVisitor {

        private final StringBuilder sb;

        public TestVisitorImpl() {
            sb = new StringBuilder();
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
            sb.append(' ');
            clause.getSecondClause().accept(this);
        }

        protected void visitImpl(final PhraseClause clause) {
            if (clause.getField() != null) {
                sb.append(clause.getField());
                sb.append(':');
            }

            sb.append(clause.getTerm());
        }

        protected void visitImpl(final Clause clause) {
            // [FIXME] implement me!
        }
    }

}
