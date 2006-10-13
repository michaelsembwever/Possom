// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.TestCase;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.mode.config.FastSearchConfiguration;
import no.schibstedsok.searchportal.result.Navigator;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.mode.command.FastSearchCommand;
import no.schibstedsok.searchportal.mode.SearchCommandFactory;
import no.schibstedsok.searchportal.util.config.DocumentLoader;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.fast.searchengine.test.MockupFastSearchEngineFactory;
import no.schibstedsok.searchportal.run.RunningQueryImpl;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;

import no.schibstedsok.searchportal.util.config.FileResourceLoader;
import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Fast navigation tests.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class FastNavigatorsTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(FastNavigatorsTest.class);

    FastSearchConfiguration config;
    MockupResultHandler resultHandler;
    
    public FastNavigatorsTest(String testName) {
        super(testName);
    }    

    public SearchConfiguration getSearchConfiguration() {
        return config;
    }

    @BeforeClass
    protected void setUp() throws Exception {

        //final FastSearchConfiguration 
                config = new FastSearchConfiguration();
        //this.config = config;
        config.setResultsToReturn(10);
        resultHandler = new MockupResultHandler();
        config.addResultHandler(resultHandler);
    }
    
    @Test
    public void testNoNavigators() {
        
        final FastSearchConfiguration config = new FastSearchConfiguration();
        assertTrue(config.getNavigators().isEmpty());
    }

    @Test
    public void testOneNavigator() {

        final Navigator navigator = new Navigator();
        navigator.setName("ywfylkesnavigator");
        config.addNavigator(navigator, "geographic");

        assertSame(config.getNavigator("geographic"), navigator);
        assertTrue(config.getNavigators().values().contains(navigator));

    }

    @Test
    public void testHierarchicalNavigator() {

        final Navigator navigator = new Navigator();
        navigator.setName("ywfylkesnavigator");

        final Navigator child = new Navigator();
        child.setName("ywkommunenavigator");

        navigator.setChildNavigator(child);

        config.addNavigator(navigator, "geographic");

        assertSame(config.getNavigator("geographic"), navigator);
        assertSame(config.getNavigator("geographic").getChildNavigator(), child);
    }

//    public void testTopLevelModifiers() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//        navigator.setDisplayName("Fylken");
//
//        config.addNavigator(navigator, "geographic");
//
//
//        final FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , new HashMap());
//
//        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//        final FastSearchResult result = (FastSearchResult) command.call();
//
//        assertTrue(result.getModifiers("geographic") != null);
//
//        final Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
//
//        assertEquals("ywfylkesnavigator", modifier.getNavigator().getName());
//        assertEquals("Fylken", command.getNavigatorTitle("geographic"));
//    }
//
//    public void testTopLevelModifiersWithChild() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//        final Navigator child = new Navigator();
//        child.setName("ywkommunenavigator");
//        navigator.setChildNavigator(child);
//
//        config.addNavigator(navigator, "geographic");
//
//
//
//        final FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , new HashMap());
//
//        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//        final FastSearchResult result = (FastSearchResult) command.call();
//
//        assertTrue(result.getModifiers("geographic") != null);
//
//        final Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
//
//        assertEquals("ywfylkesnavigator", navigator.getName());
//        assertEquals("ywfylkesnavigator", modifier.getNavigator().getName());
//    }
//
//    public void testSecondLevelModifiersWithChild() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//        final Navigator child = new Navigator();
//        child.setName("ywkommunenavigator");
//        navigator.setChildNavigator(child);
//
//        config.addNavigator(navigator, "geographic");
//
//        final String navigated[] = new String[1];
//        navigated[0] = "ywfylkesnavigator";
//
//        final Map params = new HashMap();
//
//        params.put("nav_geographic", navigated);
//
//        final FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//
//        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//        final FastSearchResult result = (FastSearchResult) command.call();
//
//        assertNotNull(result.getModifiers("geographic"));
//
//        final Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
//        assertEquals("ywkommunenavigator", modifier.getNavigator().getName());
//
//    }
//
//    public void testSecondLevelModifiersWithoutChild() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//
//        final String navigated[] = new String[1];
//        navigated[0] = "ywfylkesnavigator";
//
//        final Map params = new HashMap();
//
//        params.put("nav_geographic", navigated);
//
//        config.addNavigator(navigator, "geographic");
//
//        final FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//
//        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//        final FastSearchResult result = (FastSearchResult) command.call();
//
//        assertNotNull(result.getModifiers("geographic"));
//
//        final Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
//        assertEquals("ywfylkesnavigator", modifier.getNavigator().getName());
//
//    }
//
//    public void testThreeLevelNavigator() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//
//        final Navigator child = new Navigator();
//        child.setName("ywkommunenavigator");
//
//        final Navigator childsChild = new Navigator();
//        childsChild.setName("ywsted");
//
//        navigator.setChildNavigator(child);
//        child.setChildNavigator(childsChild);
//
//        final Map params = new HashMap();
//
//        final String navigated[] = new String[1];
//        navigated[0] = "ywfylkesnavigator";
//
//        params.put("nav_geographic", navigated);
//
//        config.addNavigator(navigator, "geographic");
//
//        FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//
//        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//        FastSearchResult result = (FastSearchResult) command.call();
//
//        assertNotNull(result.getModifiers("geographic"));
//
//        Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
//        assertEquals("ywkommunenavigator", modifier.getNavigator().getName());
//
//        params.clear();
//
//        navigated[0] = "ywkommunenavigator";
//        params.put("nav_geographic", navigated);
//
//        command = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//
//        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//        result = (FastSearchResult) command.call();
//
//        assertNotNull(result.getModifiers("geographic"));
//
//        modifier = (Modifier) result.getModifiers("geographic").get(0);
//        assertEquals("ywsted", modifier.getNavigator().getName());
//    }
//
//    public void testModifiersOneLevel() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//        navigator.setField("ywfylke");
//
//        config.addNavigator(navigator, "geographic");
//        final Map params = new HashMap();
//
//
//        final String navigated[] = new String[1];
//        navigated[0] = "ywfylkesnavigator";
//
//        final String navigatedValue[] = new String[1];
//
//        navigatedValue[0] = "Oslo";
//
//        params.put("nav_geographic", navigated);
//        params.put("ywfylke", navigatedValue);
//
//        final FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//
//        final FastSearchResult result = (FastSearchResult) command.call();
//    }
//
//    public void testModifiersTwoLevels() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//        navigator.setField("ywfylke");
//
//        final Navigator child = new Navigator();
//        child.setName("ywkommunenavigator");
//        child.setField("ywkommune");
//
//        navigator.setChildNavigator(child);
//
//        config.addNavigator(navigator, "geographic");
//        final Map params = new HashMap();
//
//        final String navigated[] = new String[1];
//        navigated[0] = "ywfylkesnavigator";
//
//        final String[] navigatedValue = new String[1];
//
//        navigatedValue[0] = "Oslo";
//
//        params.put("nav_geographic", navigated);
//        params.put("ywfylke", navigatedValue);
//
//        FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//
//        final FastSearchResult result = (FastSearchResult) command.call();
//
//        Collection filters = command.createNavigationFilterStrings();
//
//        assertTrue(filters.contains("+ywfylke:\"Oslo\""));
//
//        final String[] navigatedValue1 = new String[1];
//        navigated[0] = "ywkommunenavigator";
//        navigatedValue1[0] = "Akershus";
//
//        params.clear();
//
//        params.put("nav_geographic", navigated);
//        params.put("ywfylke", navigatedValue);
//        params.put("ywkommune", navigatedValue1);
//
//        command = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//
//        command.call();
//
//        filters = command.createNavigationFilterStrings();
//        assertTrue(filters.contains("+ywfylke:\"Oslo\""));
//        assertTrue(filters.contains("+ywkommune:\"Akershus\""));
//
//    }
//
//    public void testModifiersX() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//        navigator.setField("ywfylke");
//
//        final Navigator child = new Navigator();
//        child.setName("ywkommunenavigator");
//        child.setField("ywkommune");
//
//        navigator.setChildNavigator(child);
//
//        config.addNavigator(navigator, "geographic");
//
//        final Map params = new HashMap();
//        final String navigated[] = new String[1];
//        navigated[0] = "ywfylkesnavigator";
//
//        final String[] navigatedValue = new String[1];
//
//        navigatedValue[0] = "Oslo";
//        params.put("nav_geographic", navigated);
//        final FastSearchCommand command
//                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//
//        final FastSearchResult result = (FastSearchResult) command.call();
//
//        final Navigator nav = command.getNavigatedTo("geographic");
//
//        for (final Iterator iterator = result.getModifiers("geographic").iterator(); iterator.hasNext();) {
//            final Modifier modifier = (Modifier) iterator.next();
//        }
//    }
//
//
//    public void testHeading() {
//
//        final Navigator navigator = new Navigator();
//        navigator.setName("ywfylkesnavigator");
//        navigator.setField("ywfylke");
//
//        final Navigator child = new Navigator();
//        child.setName("ywkommunenavigator");
//        child.setField("ywkommune");
//
//        final Navigator child2 = new Navigator();
//        child2.setName("ywbydelnavigator");
//        child2.setField("ywbydel");
//
//        child.setChildNavigator(child2);
//        navigator.setChildNavigator(child);
//
//        config.addNavigator(navigator, "geographic");
//
//        final HashMap params = new HashMap();
//
//        final String navigated[] = new String[1];
//        navigated[0] = "ywfylkesnavigator";
//
//        final String navigatedValue[] = new String[1];
//
//        navigatedValue[0] = "Oslo";
//
//        params.put("nav_geographic", navigated);
//        params.put("ywfylke", navigatedValue);
//
//        {
//            final FastSearchCommand command1
//                    = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//            command1.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//
//            final FastSearchResult result = (FastSearchResult) command1.call();
//
//            LOG.info("command.getNavigatorTitle(geographic): " + command1.getNavigatorTitle("geographic"));
//            assertEquals("Oslo", command1.getNavigatorTitle("geographic"));
//        }
//
//
//        navigated[0] = "ywkommunenavigator";
//
//        final String navigatedValue2[] = new String[1];
//
//        navigatedValue2[0] = "Oslokommune";
//
//        params.clear();
//
//        params.put("nav_geographic", navigated);
//        params.put("ywfylke", navigatedValue);
//        params.put("ywkommune", navigatedValue2);
//
//        {
//            final FastSearchCommand command2
//                    = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
//            command2.setSearchEngineFactory(new MockupFastSearchEngineFactory());
//
//            command2.call();
//
//            LOG.info("command.getNavigatorTitle(geographic): " + command2.getNavigatorTitle("geographic"));
//            assertEquals("Oslokommune", command2.getNavigatorTitle("geographic"));
//        }
//    }

    public void tBackLinks() {

        final Navigator navigator = new Navigator();
        navigator.setName("ywfylkesnavigator");
        navigator.setField("ywfylke");
        navigator.setDisplayName("Fylken");
        config.addNavigator(navigator, "geographic");
        final Navigator child = new Navigator();
        child.setName("ywkommunenavigator");
        child.setField("ywkommune");
        navigator.setChildNavigator(child);

        final Navigator child2 = new Navigator();
        child2.setName("ywbydelnavigator");
        child2.setField("ywbydel");
        child.setChildNavigator(child2);

        final Map params;

        // Nothing navigated
        params = new HashMap();
        FastSearchCommand command
                = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());

        command.call();

        assertEquals(0, command.getNavigatorBackLinks("geographic").size());
        assertEquals("Fylken", command.getNavigatorTitle("geographic"));

        final String navigatedValue[] = new String[1];

        final String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        navigatedValue[0] = "Oslofylke";

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);

        command = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
        command.call();

        List links = command.getNavigatorBackLinks("geographic");
        assertEquals("Oslofylke", command.getNavigatorTitle("geographic"));

        assertEquals(0, command.getNavigatorBackLinks("geographic").size());

        final String navigatedValue2[] = new String[1];

        navigated[0] = "ywkommunenavigator";

        navigatedValue2[0] = "Oslokommune";

        params.clear();

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);
        params.put("ywkommune", navigatedValue2);

        command = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
        command.call();

        links = command.getNavigatorBackLinks("geographic");

        assertEquals(1, links.size());
        assertEquals("Oslokommune", command.getNavigatorTitle("geographic"));


        final String navigatedValue3[] = new String[1];

        navigated[0] = "ywbydelnavigator";

        navigatedValue2[0] = "Askim";

        params.clear();

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);
        params.put("ywkommune", navigatedValue2);
        params.put("ywbydel", navigatedValue3);

        command = (FastSearchCommand) SearchCommandFactory.createSearchCommand(createTestSearchCommandContext("bil") , params);
        command.call();

        links = command.getNavigatorBackLinks("geographic");

        assertEquals(1, links.size());
        assertEquals("Askim", command.getNavigatorTitle("geographic"));

    }

    private SearchCommand.Context createTestSearchCommandContext(final String query) {

        final RunningQuery.Context rqCxt = new RunningQuery.Context() {

            private final SearchMode mode = new SearchMode();

            public SearchMode getSearchMode() {
                return mode;
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.valueOf(
                    ContextWrapper.wrap(SearchTabFactory.Context.class, this))
                    .getTabByKey("d");
            }
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }

            public Site getSite() {
                return Site.DEFAULT;
            }
        };

        final RunningQuery rq = new RunningQueryImpl(rqCxt, query, new HashMap());

        final SearchCommand.Context searchCmdCxt = ContextWrapper.wrap(
                SearchCommand.Context.class,
                new BaseContext() {
                    public SearchConfiguration getSearchConfiguration() {
                        return config;
                    }
                    public RunningQuery getRunningQuery() {
                        return rq;
                    }
                    public Query getQuery(){
                        return rq.getQuery();
                    }
                },
                rqCxt);

        return searchCmdCxt;
    }

}
