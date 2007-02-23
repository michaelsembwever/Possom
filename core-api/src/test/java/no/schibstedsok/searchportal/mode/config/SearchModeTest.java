// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.site.SiteTestCase;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.mode.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.searchportal.run.RunningQueryImpl;
import java.util.HashMap;
import java.util.Properties;
import no.schibstedsok.searchportal.datamodel.DataModelTestCase;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;
import org.testng.annotations.Test;

/** SearchMode tests.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class SearchModeTest extends DataModelTestCase {

    /** Test the FastSearchConfiguration.
     **/
    @Test
    public void testFastSearchConfiguration() throws Exception {

        final SearchMode mode = new SearchMode();

        mode.setExecutor(new ParallelSearchCommandExecutor());

        final FastSearchConfiguration webCrawl = new FastSearchConfiguration();

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

        final RunningQuery.Context rqCxt = new RunningQuery.Context() {
            public SearchMode getSearchMode() {
                return mode;
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.valueOf(
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
            public Site getSite() {
                return getTestingSite();
            }
        };

        final RunningQuery query = new RunningQueryImpl(rqCxt, "aetat.no", getDataModel());

        query.run();

    }

    /** Test the OverturePPCSearchConfiguration.
     **/
    @Test
    public void testOverturePPCConfiguration() throws Exception {

        final String query = "linux";

        final SearchMode mode = new SearchMode();
        mode.setExecutor(new ParallelSearchCommandExecutor());
        final OverturePPCSearchConfiguration searchConfiguration = new OverturePPCSearchConfiguration();
        searchConfiguration.setName("test-overture-ppc-command");
        searchConfiguration.setResultsToReturn(3);
        searchConfiguration.setHost("overtureHost");
        searchConfiguration.setPort("overturePort");
        searchConfiguration.setPartnerId("overturePartnerId");
        searchConfiguration.setUrl("/d/search/p/standard/eu/xml/rlb/?mkt=se&amp;adultFilter=clean&amp;accountFilters=schibstedsok_se");
        searchConfiguration.setEncoding("UTF-8");
        mode.addSearchConfiguration(searchConfiguration);

        final RunningQuery.Context rqCxt = new RunningQuery.Context(){
            public SearchMode getSearchMode() {
                return mode;
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.valueOf(
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
            public Site getSite() {
                return getTestingSite();
            }
        };

        final RunningQuery runningQuery = new RunningQueryImpl(rqCxt, query, getDataModel());

        runningQuery.run();

    }
}

