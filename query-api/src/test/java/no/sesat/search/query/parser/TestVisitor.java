/*
 * Copyright (2005-2008) Schibsted Søk AS
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
package no.sesat.search.query.parser;


import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.search.site.SiteTestCase;
import no.sesat.search.query.token.TokenEvaluationEngineTestContext;
import no.sesat.search.query.AndNotClause;
import no.sesat.search.query.Clause;
import no.sesat.search.query.DefaultOperatorClause;
import no.sesat.search.query.LeafClause;
import no.sesat.search.query.Query;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.query.token.TokenEvaluationEngineImpl;
import no.sesat.search.site.config.*;
import no.sesat.search.query.AndClause;
import no.sesat.search.query.NotClause;
import no.sesat.search.query.OrClause;
import no.sesat.search.query.PhraseClause;
import no.sesat.search.query.WordClause;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/** Test the QueryParser and it's generated visitor pattern.
 *
 * @version $Id$
 *
 **/
public final class TestVisitor extends SiteTestCase {

    private static final Logger LOG = Logger.getLogger(TestVisitor.class);



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
                    public String getUniqueId() {
                        return "";
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
                "(((firstname:magnus AND eklund) AND oslo) (firstname:magnus AND (eklund AND oslo)) OR \"magnus eklund\" (magnus eklund))");
    }


    @Test
    public void testAndOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus AND eklund AND oslo \"magnus eklund\" 123",
                "firstname:magnus AND eklund AND oslo \"magnus eklund\" OR magnus eklund 123",
                "((((firstname:magnus AND eklund) AND oslo) (firstname:magnus AND (eklund AND oslo)) \"magnus eklund\" (magnus eklund)) 123) (((firstname:magnus AND eklund) AND oslo) (firstname:magnus AND (eklund AND oslo)) (\"magnus eklund\" (magnus eklund) 123))");
    }


    @Test
    public void testAndOrNotAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "firstname:magnus eklund oslo magnus AND eklund NOT 123",
                "firstname:magnus eklund oslo magnus AND eklund NOT 123",
                "((((firstname:magnus eklund) oslo) (magnus AND eklund)) NOT 123) (((firstname:magnus (eklund oslo)) (magnus AND eklund)) NOT 123) ((firstname:magnus (eklund (oslo (magnus AND eklund)))) NOT 123) (firstname:magnus (eklund (oslo ((magnus AND eklund) NOT 123))))");
    }


    @Test
    public void testOrAgainstQueryParser() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden gausen oldervoll nordlys",
                "((((((Hansen Inderøy) Marte) Elden) gausen) oldervoll) nordlys) (((((Hansen (Inderøy Marte)) Elden) gausen) oldervoll) nordlys) ((((Hansen (Inderøy (Marte Elden))) gausen) oldervoll) nordlys) (((Hansen (Inderøy (Marte (Elden gausen)))) oldervoll) nordlys) ((Hansen (Inderøy (Marte (Elden (gausen oldervoll))))) nordlys) (Hansen (Inderøy (Marte (Elden (gausen (oldervoll nordlys))))))");
    }


    @Test
    public void testNotOrAgainstQueryParser() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden NOT gausen oldervoll nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll nordlys",
                "((((((Hansen Inderøy) Marte) Elden) NOT gausen) oldervoll) nordlys) (((((Hansen (Inderøy Marte)) Elden) NOT gausen) oldervoll) nordlys) ((((Hansen (Inderøy (Marte Elden))) NOT gausen) oldervoll) nordlys) (((Hansen (Inderøy (Marte (Elden NOT gausen)))) oldervoll) nordlys) ((Hansen (Inderøy (Marte (Elden (NOT gausen oldervoll))))) nordlys) (Hansen (Inderøy (Marte (Elden (NOT gausen (oldervoll nordlys))))))");
    }


    @Test
    public void testAndNotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen AND Inderøy Marte AND Elden NOT gausen oldervoll nordlys",
                "Hansen AND Inderøy Marte AND Elden NOT gausen oldervoll nordlys",
                "(((((Hansen AND Inderøy) (Marte AND Elden)) NOT gausen) oldervoll) nordlys) ((((Hansen AND Inderøy) ((Marte AND Elden) NOT gausen)) oldervoll) nordlys) (((Hansen AND Inderøy) ((Marte AND Elden) (NOT gausen oldervoll))) nordlys) ((Hansen AND Inderøy) ((Marte AND Elden) (NOT gausen (oldervoll nordlys))))");
    }


    @Test
    public void testAndNotOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy Marte Elden NOT gausen oldervoll AND nordlys",
                "(((((Hansen Inderøy) Marte) Elden) NOT gausen) (oldervoll AND nordlys)) ((((Hansen (Inderøy Marte)) Elden) NOT gausen) (oldervoll AND nordlys)) (((Hansen (Inderøy (Marte Elden))) NOT gausen) (oldervoll AND nordlys)) ((Hansen (Inderøy (Marte (Elden NOT gausen)))) (oldervoll AND nordlys)) (Hansen (Inderøy (Marte (Elden (NOT gausen (oldervoll AND nordlys))))))");
    }


    //@Test
    public void testAndnotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen oldervoll nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden (gausen (oldervoll nordlys))))) (Hansen (Inderøy ANDNOT (Marte (Elden (gausen (oldervoll nordlys))))))");
    }


    //@Test
    public void testAndAndnotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden ((gausen AND oldervoll) nordlys)))) (Hansen (Inderøy ANDNOT (Marte (Elden ((gausen AND oldervoll) nordlys)))))");
    }


    //@Test
    public void testAndAndnotNotOrAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll NOT nordlys",
                "Hansen Inderøy ANDNOT Marte Elden gausen AND oldervoll NOT nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden ((gausen AND oldervoll) NOT nordlys)))) (Hansen (Inderøy ANDNOT (Marte (Elden ((gausen AND oldervoll) NOT nordlys)))))");
    }


    //@Test
    public void testAndAndnotNotOrAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "Hansen Inderøy ANDNOT Marte Elden NOT gausen oldervoll AND nordlys",
                "Hansen Inderøy ANDNOT Marte Elden NOT gausen oldervoll AND nordlys",
                "((Hansen Inderøy) ANDNOT (Marte (Elden (NOT gausen (oldervoll AND nordlys))))) (Hansen (Inderøy ANDNOT (Marte (Elden (NOT gausen (oldervoll AND nordlys))))))");
    }


    @Test
    public void testPhoneNumberAgainstQueryParser1() {
        basicQueryParserWithTestVisitorImpl(
                "92221689",
                "92221689 OR 92221689",
                "92221689 92221689");
    }


    @Test
    public void testPhoneNumberAgainstQueryParser2() {
        basicQueryParserWithTestVisitorImpl(
                "9222 1689",
                "92221689 OR 9222 1689",
                "92221689 92221689 (9222 1689)");
    }


    @Test
    public void testPhoneNumberAgainstQueryParser3() {
        basicQueryParserWithTestVisitorImpl(
                "+47 9222 1689",
                "+4792221689 OR 47 9222 1689",
                "+4792221689 4792221689 ((47 9222) 1689) (47 (9222 1689))");
    }


    @Test
    public void testUnicodeAgainstQueryParser3() {
        basicQueryParserWithTestVisitorImpl(
                "सिद्धार्थ गौतम",
                "सिद्धार्थ गौतम",
                "(सिद्धार्थ गौतम)");
    }

    /** See SKER4723 **/
    @Test
    public void testPhraseBeforeOrOperationPrecedence(){


          basicQueryParserWithTestVisitorImpl(
            "test \"4W-Moto Snøscooter for barn (NY)\"",
            "92221689 OR 92221689",
            "(test \"4W-Moto Snøscooter for barn (NY)\" (((((4W Moto) Snøscooter) for) barn) NY) ((((4W (Moto Snøscooter)) for) barn) NY) (((4W (Moto (Snøscooter for))) barn) NY) ((4W (Moto (Snøscooter (for barn)))) NY) (4W (Moto (Snøscooter (for (barn NY))))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Massey Ferguson 165 (AD 4-212)\"",
            "92221689 OR 92221689",
            "(test \"Massey Ferguson 165 (AD 4-212)\" ((((Massey Ferguson) 165) AD) 4212 (4 212)) (((Massey (Ferguson 165)) AD) 4212 (4 212)) ((Massey (Ferguson (165 AD))) 4212 (4 212)) (Massey (Ferguson (165 (AD 4212 (4 212))))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Volkswagen Boble (gml. type)\"",
            "92221689 OR 92221689",
            "(test \"Volkswagen Boble (gml. type)\" (((Volkswagen Boble) gml.) type) ((Volkswagen (Boble gml.)) type) (Volkswagen (Boble (gml. type))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"AMT 170 R ( ny)\"",
            "92221689 OR 92221689",
            "(test \"AMT 170 R ( ny)\" (((AMT 170) R) ny) ((AMT (170 R)) ny) (AMT (170 (R ny))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Winga Winga 29 (Ohlson 29)\"",
            "92221689 OR 92221689",
            "(test \"Winga Winga 29 (Ohlson 29)\" ((((Winga Winga) 29) Ohlson) 29) (((Winga (Winga 29)) Ohlson) 29) ((Winga (Winga (29 Ohlson))) 29) (Winga (Winga (29 (Ohlson 29)))))");


          basicQueryParserWithTestVisitorImpl(
            "test \"Nidelv (725) Super Sport R\"",
            "92221689 OR 92221689",
            "(test \"Nidelv (725) Super Sport R\" ((((Nidelv 725) Super) Sport) R) (((Nidelv (725 Super)) Sport) R) ((Nidelv (725 (Super Sport))) R) (Nidelv (725 (Super (Sport R)))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Saga 29 LS (CABIN)\"",
            "92221689 OR 92221689",
            "(test \"Saga 29 LS (CABIN)\" (((Saga 29) LS) CABIN) ((Saga (29 LS)) CABIN) (Saga (29 (LS CABIN))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Viksund St.Cruz 310 (nå 340)\"",
            "92221689 OR 92221689",
            "(test \"Viksund St.Cruz 310 (nå 340)\" ((((Viksund St.Cruz) 310) nå) 340) (((Viksund (St.Cruz 310)) nå) 340) ((Viksund (St.Cruz (310 nå))) 340) (Viksund (St.Cruz (310 (nå 340)))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Volkswagen Boble (gml. type)\"",
            "92221689 OR 92221689",
            "(test \"Volkswagen Boble (gml. type)\" (((Volkswagen Boble) gml.) type) ((Volkswagen (Boble gml.)) type) (Volkswagen (Boble (gml. type))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Fiat 2,3 sloop1 (120 Hk )\"",
            "92221689 OR 92221689",
            "(test \"Fiat 2,3 sloop1 (120 Hk )\" (((((Fiat 2) 3) sloop1) 120) Hk) ((((Fiat (2 3)) sloop1) 120) Hk) (((Fiat (2 (3 sloop1))) 120) Hk) ((Fiat (2 (3 (sloop1 120)))) Hk) (Fiat (2 (3 (sloop1 (120 Hk))))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"AMT 170 R ( ny)\"",
            "92221689 OR 92221689",
            "(test \"AMT 170 R ( ny)\" (((AMT 170) R) ny) ((AMT (170 R)) ny) (AMT (170 (R ny))))");

          basicQueryParserWithTestVisitorImpl(
            "test \"Massey Ferguson 165 (A 4.212)-2\"",
            "92221689 OR 92221689",
            "(test \"Massey Ferguson 165 (A 4.212)-2\" ((((Massey Ferguson) 165) A) 42122 (4.212 2)) (((Massey (Ferguson 165)) A) 42122 (4.212 2)) ((Massey (Ferguson (165 A))) 42122 (4.212 2)) (Massey (Ferguson (165 (A 42122 (4.212 2))))))");

    }

    /** See SKER4723 **/
    @Test
    public void testEarlyBirdFieldSeparator(){

          basicQueryParserWithTestVisitorImpl(
            ": «Always There»",
            "92221689 OR 92221689",
            "(Always There)");

          basicQueryParserWithTestVisitorImpl(
            ":http://www.nordealiv.no/bedriftsdialog",
            "92221689 OR 92221689",
            "http://www.nordealiv.no/bedriftsdialog");

          basicQueryParserWithTestVisitorImpl(
            "://www.bodo.se/djur/haest_fjording.htm",
            "92221689 OR 92221689",
            "((www.bodo.se djur) haest_fjording.htm) (www.bodo.se (djur haest_fjording.htm))");
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
