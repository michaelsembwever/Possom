/* Copyright (2006-2008) Schibsted ASA
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
package no.sesat.search.mode.command;


import no.sesat.search.result.test.*;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelTestCase;
import no.sesat.search.mode.command.SearchCommand;
import no.sesat.search.mode.config.BaseSearchConfiguration;
import no.sesat.search.mode.config.FastCommandConfig;
import no.sesat.search.result.Navigator;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.query.Query;
import no.sesat.search.run.RunningQuery;
import no.sesat.search.run.RunningQueryImpl;
import no.sesat.search.site.config.*;
import no.sesat.search.site.SiteContext;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.SearchTabFactory;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


/** Fast navigation tests.
 *
 *
 * @version <tt>$Id$</tt>
 */
public final class FastNavigatorsTest extends DataModelTestCase {

    private static final Logger LOG = Logger.getLogger(FastNavigatorsTest.class);

    FastCommandConfig config;
    MockupResultHandler resultHandler;

    /**
     *
     * @return
     */
    public BaseSearchConfiguration getSearchConfiguration() {
        return config;
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    @Override
    protected void setUp() throws Exception {

        //final FastCommandConfig
                config = new FastCommandConfig();
        //this.config = config;
        config.setResultsToReturn(10);
//        resultHandler = new MockupResultHandler();
//        config.addResultHandler(resultHandler);
    }

    /**
     *
     */
    @Test
    public void testNoNavigators() {

        final FastCommandConfig config = new FastCommandConfig();
        assertTrue(config.getNavigators().isEmpty());
    }

    /**
     *
     */
    @Test
    public void testOneNavigator() {

        final Navigator navigator = new Navigator();
        navigator.setName("ywfylkesnavigator");
        config.addNavigator(navigator, "geographic");

        assertSame(config.getNavigator("geographic"), navigator);
        assertTrue(config.getNavigators().values().contains(navigator));

    }

    /**
     *
     */
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

    private SearchCommand.Context createTestSearchCommandContext(final String query) throws Exception{

        final DataModel datamodel = getDataModel();

        final RunningQuery.Context rqCxt = new RunningQuery.Context() {

            private final SearchMode mode = new SearchMode();

            public SearchMode getSearchMode() {
                return mode;
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.instanceOf(
                    ContextWrapper.wrap(SearchTabFactory.Context.class, this))
                    .getTabByKey("d");
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

            public DataModel getDataModel(){
                return datamodel;
            }
        };

        final RunningQuery rq = new RunningQueryImpl(rqCxt, query);

        final SearchCommand.Context searchCmdCxt = ContextWrapper.wrap(
                SearchCommand.Context.class,
                new BaseContext() {
                    public BaseSearchConfiguration getSearchConfiguration() {
                        return config;
                    }
                    public RunningQuery getRunningQuery() {
                        return rq;
                    }
                    public Query getQuery(){
                        return datamodel.getQuery().getQuery();
                    }
                },
                rqCxt);

        return searchCmdCxt;
    }

}
