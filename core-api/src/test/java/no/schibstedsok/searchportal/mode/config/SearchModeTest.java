// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.mode.config;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.TestCase;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.mode.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.searchportal.run.RunningQueryImpl;

import java.util.HashMap;
import java.util.Properties;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;
import org.testng.annotations.Test;

/** SearchMode tests.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 3359 $</tt>
 */
public class SearchModeTest extends TestCase {

    public SearchModeTest(final String testName) {
        super(testName);
    }	
    
    /** Test the WebCrawl index.
     **/
    @Test
    public void testWebCrawl() {

        final SearchMode mode = new SearchMode();

        mode.setExecutor(new ParallelSearchCommandExecutor());

        final FastSearchConfiguration webCrawl = new FastSearchConfiguration();

        webCrawl.setQueryServerURL("http://localhost:15100");
        webCrawl.addCollection("webcrawlno1");
        webCrawl.addCollection("webcrawlno1deep1");
        webCrawl.addCollection("webcrawlno2");
//        webCrawl.addResultHandler(new TextOutputResultHandler());
        webCrawl.addResultField("url");
        webCrawl.addResultField("title");
        webCrawl.addResultField("body");
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
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }
            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }
            public Site getSite() {
                return Site.DEFAULT;
            }
            public SearchTabFactory getLeafSearchTabFactory(){
                return null;
            }
        };

        final RunningQuery query = new RunningQueryImpl(rqCxt, "aetat.no", new HashMap());

        try {
            query.run();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    @Test
    public void testOverturePPCConfiguration() throws Exception {

        final String query = "linux";

        final SearchMode mode = new SearchMode();
        mode.setExecutor(new ParallelSearchCommandExecutor());
        final SearchConfiguration searchConfiguration = new OverturePPCSearchConfiguration();
        searchConfiguration.setResultsToReturn(3);
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
            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this,resource, properties);
            }

            public DocumentLoader newDocumentLoader(final String resource, final DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }

            public Site getSite() {
                return Site.DEFAULT;
            }
            public SearchTabFactory getLeafSearchTabFactory(){
                return null;
            }
        };

        final RunningQuery runningQuery = new RunningQueryImpl(rqCxt, query, new HashMap());

        runningQuery.run();

    }
}

