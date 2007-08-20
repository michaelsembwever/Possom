/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 *
 * Created on March 4, 2006, 2:32 PM
 *
 */

package no.sesat.search.mode.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import no.sesat.search.mode.SearchCommandFactory;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.mode.executor.SearchCommandExecutorFactory;
import no.sesat.search.run.RunningQuery;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeLoader;
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

    
    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testAllNorskNettsokSearchCommands() throws Exception{
        executeTestOfQuery("linux", "d");
    }

    
    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testAllInternasjonalNettsokSearchCommands() throws Exception{

        executeTestOfQuery("linux", "g");
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testAllWhitepagesSearchCommands() throws Exception{

        executeTestOfQuery("linux", "w");
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testAllYellowpagesSearchCommands() throws Exception{

        executeTestOfQuery("linux", "y");
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testAllNyheterSearchCommands() throws Exception{

        executeTestOfQuery("linux", "m");
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testAllBilderSearchCommands() throws Exception{

        executeTestOfQuery("linux", "p");
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    @Test
    public void testAllTvSearchCommands() throws Exception{
        executeTestOfQuery("linux", "t");
    }
    
    private void executeTestOfQuery(
            final String query, 
            final String key) throws SiteKeyedFactoryInstantiationException{

        // proxy it back to the RunningQuery context.
        final RunningQuery.Context rqCxt = createRunningQueryContext(key);
        
        updateAttributes(rqCxt.getDataModel().getJunkYard().getValues(), rqCxt);
        final RunningTestQuery rq = new RunningTestQuery(rqCxt, query);
        rqCxt.getDataModel().getJunkYard().getValues().put("query", rq);

        final Collection<SearchCommand> commands 
                = new ArrayList<SearchCommand>();

        final SearchCommandFactory.Context commandFactoryContext = new SearchCommandFactory.Context() {
            public Site getSite() {
                return rqCxt.getDataModel().getSite().getSite();
            }

            public BytecodeLoader newBytecodeLoader(final SiteContext site, final String name, final String jar) {
                return rqCxt.newBytecodeLoader(site, name, jar);
            }
        };

        final SearchCommandFactory commandFactory = new SearchCommandFactory(commandFactoryContext);

        for(SearchConfiguration conf : rqCxt.getSearchMode().getSearchConfigurations()){
            

            LOG.info(DEBUG_EXECUTE_COMMAND + conf.getName());

            final SearchCommand.Context cxt = createCommandContext(rq, rqCxt, conf.getName());

            final SearchCommand cmd = commandFactory.getController(cxt);

            commands.add(cmd);
        }
        try{

            SearchCommandExecutorFactory.getController(rqCxt.getSearchMode().getExecutor()).invokeAll(commands);
            
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
        map.put("no.sesat.Statistics", new StringBuffer());
        
        //final Properties props = SiteConfiguration.valueOf(
        //                ContextWrapper.wrap(SiteConfiguration.Context.class, rqCxt)).getProperties();
        //
        //map.set("linkpulse", new Linkpulse(rqCxt.getSite(), props));
        return map;
    }

}