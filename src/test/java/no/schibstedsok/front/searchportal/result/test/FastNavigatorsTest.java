package no.schibstedsok.front.searchportal.result.test;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.FastConfiguration;
import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.command.FastSearchCommand;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.fast.searchengine.test.MockupFastSearchEngineFactory;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.Modifier;

import java.util.*;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastNavigatorsTest extends TestCase {

    FastConfiguration config;
    MockupResultHandler resultHandler;

    protected void setUp() throws Exception {
        FastConfiguration config = new FastConfiguration();
        this.config = config;
        config.setResultsToReturn(10);
        resultHandler = new MockupResultHandler();
        config.addResultHandler(resultHandler);
    }

    public void testNoNavigators() {
        assertTrue(config.getNavigators().isEmpty());
    }

    public void testOneNavigator() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        config.addNavigator(navigator, "geographic");

        assertSame(config.getNavigator("geographic"), navigator);
        assertTrue(config.getNavigators().values().contains(navigator));

    }

    public void testHierarchicalNavigator() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");

        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");

        navigator.setChildNavigator(child);

        config.addNavigator(navigator, "geographic");

        assertSame(config.getNavigator("geographic"), navigator);
        assertSame(config.getNavigator("geographic").getChildNavigator(), child);
    }

    public void testTopLevelModifiers() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        navigator.setDisplayName("Fylken");

        config.addNavigator(navigator, "geographic");
        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , new HashMap());
        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
        FastSearchResult result = (FastSearchResult) command.call();

        assertTrue(result.getModifiers("geographic") != null);

        Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);

        assertEquals("ywfylkesnavigator", modifier.getNavigator().getName());
        assertEquals("Fylken", command.getNavigatorTitle("geographic"));
    }

    public void testTopLevelModifiersWithChild() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");
        navigator.setChildNavigator(child);

        config.addNavigator(navigator, "geographic");
        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , new HashMap());
        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
        FastSearchResult result = (FastSearchResult) command.call();

        assertTrue(result.getModifiers("geographic") != null);

        Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);

        assertEquals("ywfylkesnavigator", navigator.getName());
        assertEquals("ywfylkesnavigator", modifier.getNavigator().getName());
    }

    public void testSecondLevelModifiersWithChild() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");
        navigator.setChildNavigator(child);

        config.addNavigator(navigator, "geographic");

        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        Map params = new HashMap();

        params.put("nav_geographic", navigated);

        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);

        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
        FastSearchResult result = (FastSearchResult) command.call();

        assertNotNull(result.getModifiers("geographic"));

        Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
        assertEquals("ywkommunenavigator", modifier.getNavigator().getName());

    }

    public void testSecondLevelModifiersWithoutChild() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");

        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        Map params = new HashMap();

        params.put("nav_geographic", navigated);

        config.addNavigator(navigator, "geographic");

        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);

        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
        FastSearchResult result = (FastSearchResult) command.call();

        assertNotNull(result.getModifiers("geographic"));

        Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
        assertEquals("ywfylkesnavigator", modifier.getNavigator().getName());

    }

    public void testThreeLevelNavigator() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");

        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");

        FastNavigator childsChild = new FastNavigator();
        childsChild.setName("ywsted");

        navigator.setChildNavigator(child);
        child.setChildNavigator(childsChild);

        Map params = new HashMap();

        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        params.put("nav_geographic", navigated);

        config.addNavigator(navigator, "geographic");

        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);

        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
        FastSearchResult result = (FastSearchResult) command.call();

        assertNotNull(result.getModifiers("geographic"));

        Modifier modifier = (Modifier) result.getModifiers("geographic").get(0);
        assertEquals("ywkommunenavigator", modifier.getNavigator().getName());

        params.clear();

        navigated[0] = "ywkommunenavigator";
        params.put("nav_geographic", navigated);

        command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);

        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());
        result = (FastSearchResult) command.call();

        assertNotNull(result.getModifiers("geographic"));

        modifier = (Modifier) result.getModifiers("geographic").get(0);
        assertEquals("ywsted", modifier.getNavigator().getName());
    }

    public void testModifiersOneLevel() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        navigator.setField("ywfylke");

        config.addNavigator(navigator, "geographic");
        Map params = new HashMap();


        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        String navigatedValue[] = new String[1];

        navigatedValue[0] = "Oslo";

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);

        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);

        FastSearchResult result = (FastSearchResult) command.call();
    }

    public void testModifiersTwoLevels() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        navigator.setField("ywfylke");

        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");
        child.setField("ywkommune");

        navigator.setChildNavigator(child);

        config.addNavigator(navigator, "geographic");
        Map params = new HashMap();

        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        String[] navigatedValue = new String[1];

        navigatedValue[0] = "Oslo";

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);

        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);

        FastSearchResult result = (FastSearchResult) command.call();

        Collection filters = command.createNavigationFilterStrings();

        assertTrue(filters.contains("+ywfylke:\"Oslo\""));

        String[] navigatedValue1 = new String[1];
        navigated[0] = "ywkommunenavigator";
        navigatedValue1[0] = "Akershus";

        params.clear();

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);
        params.put("ywkommune", navigatedValue1);

        command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);

        command.call();

        filters = command.createNavigationFilterStrings();
        assertTrue(filters.contains("+ywfylke:\"Oslo\""));
        assertTrue(filters.contains("+ywkommune:\"Akershus\""));

    }

    public void testModifiersX() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        navigator.setField("ywfylke");

        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");
        child.setField("ywkommune");

        navigator.setChildNavigator(child);

        config.addNavigator(navigator, "geographic");

        Map params = new HashMap();
        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        String[] navigatedValue = new String[1];

        navigatedValue[0] = "Oslo";
        params.put("nav_geographic", navigated);
        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);
        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());

        FastSearchResult result = (FastSearchResult) command.call();

        FastNavigator nav = command.getNavigatedTo("geographic");

        for (Iterator iterator = result.getModifiers("geographic").iterator(); iterator.hasNext();) {
            Modifier modifier = (Modifier) iterator.next();
            System.out.println(nav.getField() + "=" + modifier.getName());
        }
    }


    public void testHeading() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        navigator.setField("ywfylke");

        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");
        child.setField("ywkommune");

        FastNavigator child2 = new FastNavigator();
        child2.setName("ywbydelnavigator");
        child2.setField("ywbydel");

        child.setChildNavigator(child2);
        navigator.setChildNavigator(child);

        config.addNavigator(navigator, "geographic");

        HashMap params = new HashMap();

        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        String navigatedValue[] = new String[1];

        navigatedValue[0] = "Oslo";

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);

        FastSearchCommand command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);
        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());

        FastSearchResult result = (FastSearchResult) command.call();

        assertEquals("Oslo", command.getNavigatorTitle("geographic"));

        navigated[0] = "ywkommunenavigator";

        String navigatedValue2[] = new String[1];

        navigatedValue2[0] = "Oslokommune";

        params.clear();

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);
        params.put("ywkommune", navigatedValue2);

        command.call();

        assertEquals("Oslokommune", command.getNavigatorTitle("geographic"));
    }

    public void tBackLinks() {
        FastNavigator navigator = new FastNavigator();
        navigator.setName("ywfylkesnavigator");
        navigator.setField("ywfylke");
        navigator.setDisplayName("Fylken");
        config.addNavigator(navigator, "geographic");
        FastNavigator child = new FastNavigator();
        child.setName("ywkommunenavigator");
        child.setField("ywkommune");
        navigator.setChildNavigator(child);

        FastNavigator child2 = new FastNavigator();
        child2.setName("ywbydelnavigator");
        child2.setField("ywbydel");
        child.setChildNavigator(child2);

        Map params;

        // Nothing navigated
        params = new HashMap();
        FastSearchCommand command;
        command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);
        command.setSearchEngineFactory(new MockupFastSearchEngineFactory());

        command.call();

        assertEquals(0, command.getNavigatorBackLinks("geographic").size());
        assertEquals("Fylken", command.getNavigatorTitle("geographic"));

        String navigatedValue[] = new String[1];

        String navigated[] = new String[1];
        navigated[0] = "ywfylkesnavigator";

        navigatedValue[0] = "Oslofylke";

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);

        command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);
        command.call();

        List links = command.getNavigatorBackLinks("geographic");
        assertEquals("Oslofylke", command.getNavigatorTitle("geographic"));

        assertEquals(0, command.getNavigatorBackLinks("geographic").size());

        String navigatedValue2[] = new String[1];

        navigated[0] = "ywkommunenavigator";

        navigatedValue2[0] = "Oslokommune";

        params.clear();

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);
        params.put("ywkommune", navigatedValue2);

        command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);
        command.call();

        links = command.getNavigatorBackLinks("geographic");

        assertEquals(1, links.size());
        assertEquals("Oslokommune", command.getNavigatorTitle("geographic"));


        String navigatedValue3[] = new String[1];

        navigated[0] = "ywbydelnavigator";

        navigatedValue2[0] = "Askim";

        params.clear();

        params.put("nav_geographic", navigated);
        params.put("ywfylke", navigatedValue);
        params.put("ywkommune", navigatedValue2);
        params.put("ywbydel", navigatedValue3);

        command = (FastSearchCommand) config.createCommand(new RunningQuery(new SearchMode(), "bil", new HashMap()) , params);
        command.call();

        links = command.getNavigatorBackLinks("geographic");

        assertEquals(1, links.size());
        assertEquals("Askim", command.getNavigatorTitle("geographic"));

    }

}
