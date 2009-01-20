/* Copyright (2006-2008) Schibsted Søk AS
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
/*
 *
 * Created on March 4, 2006, 2:32 PM
 *
 */

package no.sesat.search.mode.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.mode.SearchCommandFactory;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.mode.executor.SearchCommandExecutorFactory;
import no.sesat.search.run.RunningQuery;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.view.SearchTabFactory;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.navigation.ResultPagingNavigationConfig;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;


/** Executes all search commands in the given different tabs.
 * Corresponding to that tab's mode.
 *
 *
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
    public void testAllSearchCommands() throws Exception{

        final DataModel datamodel = getDataModel();

        final SiteContext siteCxt = new SiteContext(){
            public Site getSite() {
                return datamodel.getSite().getSite();
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

        };

        final SearchTabFactory stFactory = SearchTabFactory.instanceOf(
            ContextWrapper.wrap(SearchTabFactory.Context.class, siteCxt));

        for(SearchTab tab : stFactory.getTabsByName().values()){
            if(null != tab.getKey()){
                executeTestOfQuery("linux", tab.getKey());
            }
        }
    }

    private void executeTestOfQuery(
            final String query,
            final String key) throws SiteKeyedFactoryInstantiationException{



        // proxy it back to the RunningQuery context.
        final RunningQuery.Context rqCxt = createRunningQueryContext(key);

        getDataModelFactory().assignControlLevel(rqCxt.getDataModel(), ControlLevel.RUNNING_QUERY_CONSTRUCTION);

        final RunningTestQuery rq = new RunningTestQuery(rqCxt, query);

        getDataModelFactory().assignControlLevel(rqCxt.getDataModel(), ControlLevel.SEARCH_COMMAND_CONSTRUCTION);

        final Collection<SearchCommand> commands = new ArrayList<SearchCommand>();

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

            LOG.info(DEBUG_EXECUTE_COMMAND + conf.getId());

            final SearchCommand.Context cxt = createCommandContext(rq, rqCxt, conf.getId());

            final SearchCommand cmd = commandFactory.getController(cxt);

            commands.add(cmd);
        }
        try{
            LOG.info("Invoking all " + commands.size() + " commands in " + rqCxt.getSearchMode());

            getDataModelFactory().assignControlLevel(rqCxt.getDataModel(), ControlLevel.SEARCH_COMMAND_EXECUTION);

            SearchCommandExecutorFactory.getController(rqCxt.getSearchMode().getExecutor()).invokeAll(commands);

        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        }
    }

}