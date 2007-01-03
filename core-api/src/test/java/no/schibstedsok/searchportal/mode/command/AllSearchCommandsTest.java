// Copyright (2006) Schibsted Søk AS
/*
 *
 * Created on March 4, 2006, 2:32 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import no.schibstedsok.searchportal.mode.SearchCommandFactory;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.run.RunningQueryImpl;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;


/** Executes all search commands in the given different tabs.
 * Corresponding to that tab's mode.
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class AllSearchCommandsTest extends AbstractSearchCommandTest {

    private static final Logger LOG = Logger.getLogger(AllSearchCommandsTest.class);

    private static final String DEBUG_EXECUTE_COMMAND = "Testing command ";

    public AllSearchCommandsTest(final String name) {
        super(name);
    }

    
    @Test
    public void testAllNorskNettsokSearchCommands() {
        executeTestOfQuery("linux", "d");
    }

    
    @Test
    public void testAllInternasjonalNettsokSearchCommands() {

        executeTestOfQuery("linux", "g");
    }

    @Test
    public void testAllWhitepagesSearchCommands() {

        executeTestOfQuery("linux", "w");
    }

    @Test
    public void testAllYellowpagesSearchCommands() {

        executeTestOfQuery("linux", "y");
    }

    @Test
    public void testAllNyheterSearchCommands() {

        executeTestOfQuery("linux", "m");
    }

    @Test
    public void testAllBilderSearchCommands() {

        executeTestOfQuery("linux", "p");
    }

    @Test
    public void testAllTvSearchCommands() {
        executeTestOfQuery("linux", "t");
    }
    
    private void executeTestOfQuery(final String query, final String key) {

        // proxy it back to the RunningQuery context.
        final RunningQuery.Context rqCxt = createRunningQueryContext(key);
        
        final Map<String,Object> map = updateAttributes(new HashMap<String,Object>(), rqCxt);
        final RunningQuery rq = new RunningQueryImpl(rqCxt, query, map);

        final Collection<Callable<SearchResult>> commands = new ArrayList<Callable<SearchResult>>();

        for(SearchConfiguration conf : rqCxt.getSearchMode().getSearchConfigurations()){

            LOG.info(DEBUG_EXECUTE_COMMAND + conf.getName());

            final SearchCommand.Context cxt = createCommandContext(query, rqCxt, conf.getName());

            final SearchCommand cmd = SearchCommandFactory.createSearchCommand(cxt, map);

            commands.add(cmd);
        }
        try{

            rqCxt.getSearchMode().getExecutor()
                    .invokeAll(commands, new HashMap<String, Future<SearchResult>>(), Integer.MAX_VALUE);
            
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }
    
    /** Matchs the same method in SearchServlet. **/
    private static Map<String,Object> updateAttributes(
            final Map<String,Object> map,
            final RunningQuery.Context rqCxt){
        
        
        if (map.get("offset") == null || "".equals(map.get("offset"))) {
            map.put("offset", "0");
        }

        map.put("contextPath", "/");
        //map.set("tradedoubler", new TradeDoubler(request));
        map.put("no.schibstedsok.Statistics", new StringBuffer());
        
        //final Properties props = SiteConfiguration.valueOf(
        //                ContextWrapper.wrap(SiteConfiguration.Context.class, rqCxt)).getProperties();
        //
        //map.set("linkpulse", new Linkpulse(rqCxt.getSite(), props));
        return map;
    }

}