/* Copyright (2006-2007) Schibsted Søk AS
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
package no.sesat.search.mode.config;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.run.RunningQuery;
import no.sesat.search.run.RunningQueryImpl;
import java.util.Properties;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelFactory;
import no.sesat.search.datamodel.DataModelTestCase;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.SearchTabFactory;
import org.testng.annotations.Test;

/** SearchMode tests.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchModeTest extends DataModelTestCase {

    /** Test the FastCommandConfig.
     ** @throws java.lang.Exception
     */
    @Test
    public void testFastCommandConfig() throws Exception {

        final SearchMode mode = new SearchMode();

        mode.setExecutor(SearchMode.SearchCommandExecutorConfig.PARALLEL);

        final FastCommandConfig webCrawl = new FastCommandConfig();

        webCrawl.setName("test-fast-search-configuration");
        webCrawl.setQueryServerUrl("queryServerURL.1");
        webCrawl.addCollection("webcrawlno1");
        webCrawl.addCollection("webcrawlno1deep1");
        webCrawl.addCollection("webcrawlno2");
//        webCrawl.addResultHandler(new TextOutputResultHandler());
        webCrawl.addResultField("url");
        webCrawl.addResultField("title");
        webCrawl.addResultField("body");
        webCrawl.setSpellchecklanguage("no");
        webCrawl.setResultsToReturn(10);

        mode.addSearchConfiguration(webCrawl);

        final DataModel datamodel = getDataModel();

        final RunningQuery.Context rqCxt = new RunningQuery.Context() {
            public SearchMode getSearchMode() {
                return mode;
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.instanceOf(
                    ContextWrapper.wrap(SearchTabFactory.Context.class,
                    this,
                    new SiteContext(){
                        public Site getSite(){
                            return datamodel.getSite().getSite();
                        }
                    }))
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

        // DataModel's ControlLevel will be REQUEST_CONSTRUCTION
        //  Increment it onwards to RUNNING_QUERY_CONSTRUCTION.
        DataModelFactory
                .instanceOf(ContextWrapper.wrap(
                DataModelFactory.Context.class, 
                rqCxt,
                new SiteContext(){
                    public Site getSite(){
                        return datamodel.getSite().getSite();
                    }
                }))
                .assignControlLevel(datamodel, ControlLevel.RUNNING_QUERY_CONSTRUCTION);
        
        final RunningQuery query = new RunningQueryImpl(rqCxt, "aetat.no");

        query.run();

    }

    /** Test the OverturePPCCommandConfig.
     ** @throws java.lang.Exception
     */
    @Test
    public void testOverturePPCConfiguration() throws Exception {

        final String query = "linux";

        final SearchMode mode = new SearchMode();
        mode.setExecutor(SearchMode.SearchCommandExecutorConfig.PARALLEL);
        final OverturePpcCommandConfig searchConfiguration = new OverturePpcCommandConfig();
        searchConfiguration.setName("test-overture-ppc-command");
        searchConfiguration.setResultsToReturn(3);
        searchConfiguration.setHost("overtureHost");
        searchConfiguration.setPort("overturePort");
        searchConfiguration.setPartnerId("overturePartnerId");
        searchConfiguration.setUrl("/d/search/p/standard/eu/xml/rlb/?mkt=se&amp;adultFilter=clean&amp;accountFilters=schibstedsok_se");
        searchConfiguration.setEncoding("UTF-8");
        mode.addSearchConfiguration(searchConfiguration);

        final DataModel datamodel = getDataModel();

        final RunningQuery.Context rqCxt = new RunningQuery.Context(){
            public SearchMode getSearchMode() {
                return mode;
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.instanceOf(
                    ContextWrapper.wrap(SearchTabFactory.Context.class,
                    this,
                    new SiteContext(){
                        public Site getSite(){
                            return datamodel.getSite().getSite();
                        }
                    }))
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

        // DataModel's ControlLevel will be REQUEST_CONSTRUCTION
        //  Increment it onwards to RUNNING_QUERY_CONSTRUCTION.
        DataModelFactory
                .instanceOf(ContextWrapper.wrap(
                DataModelFactory.Context.class, 
                rqCxt,
                new SiteContext(){
                    public Site getSite(){
                        return datamodel.getSite().getSite();
                    }
                }))
                .assignControlLevel(datamodel, ControlLevel.RUNNING_QUERY_CONSTRUCTION);
        
        final RunningQuery runningQuery = new RunningQueryImpl(rqCxt, query);

        runningQuery.run();

    }
}

