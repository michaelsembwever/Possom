// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.configuration;

import com.thoughtworks.xstream.XStream;
import javax.xml.parsers.DocumentBuilder;
import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.front.searchportal.result.TextOutputResultHandler;

import java.util.HashMap;
import java.util.Properties;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.XStreamLoader;
import no.schibstedsok.front.searchportal.site.Site;

/** SearchMode tests.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchModeTest extends TestCase {

    /** Test the WebCrawl index.
     **/
    public void testWebCrawl() {

        final SearchMode mode = new SearchMode();

        mode.setExecutor(new ParallelSearchCommandExecutor());

        final FastConfiguration webCrawl = new FastConfiguration();

        webCrawl.setQueryServerURL("http://localhost:15100");
        webCrawl.addCollection("webcrawlno1");
        webCrawl.addCollection("webcrawlno1deep1");
        webCrawl.addCollection("webcrawlno2");
        webCrawl.addResultHandler(new TextOutputResultHandler());
        webCrawl.addResultField("url");
        webCrawl.addResultField("title");
        webCrawl.addResultField("body");
        webCrawl.setResultsToReturn(10);

        mode.addSearchConfiguration(webCrawl);

        final RunningQuery.Context rqCxt = new RunningQuery.Context() {
            public SearchMode getSearchMode() {
                return mode;
            }

            public PropertiesLoader newPropertiesLoader(final String resource, final Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this, resource, properties);
            }

            public XStreamLoader newXStreamLoader(final String resource, final XStream xstream) {
                return FileResourceLoader.newXStreamLoader(this, resource, xstream);
            }
            
            public DocumentLoader newDocumentLoader(String resource, DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }

            public Site getSite() {
                return Site.DEFAULT;
            }

        };

        final RunningQuery query = new RunningQuery(rqCxt, "aetat.no", new HashMap());

        try {
            query.run();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    

    public void testOverturePPCConfiguration() throws Exception {

        final String query = "linux";

        final SearchMode mode = new SearchMode();
        mode.setExecutor(new ParallelSearchCommandExecutor());
        final SearchConfiguration searchConfiguration = new OverturePPCConfiguration();
        searchConfiguration.setResultsToReturn(3);
        mode.addSearchConfiguration(searchConfiguration);

        final RunningQuery.Context rqCxt = new RunningQuery.Context(){
            public SearchMode getSearchMode() {
                return mode;
            }

            public PropertiesLoader newPropertiesLoader(String resource, Properties properties) {
                return FileResourceLoader.newPropertiesLoader(this,resource, properties);
            }

            public XStreamLoader newXStreamLoader(String resource, XStream xstream) {
                return FileResourceLoader.newXStreamLoader(this,resource, xstream);
            }
            
            public DocumentLoader newDocumentLoader(String resource, DocumentBuilder builder) {
                return FileResourceLoader.newDocumentLoader(this, resource, builder);
            }

            public Site getSite() {
                return Site.DEFAULT; 
            }
            
        };
        
        final RunningQuery runningQuery = new RunningQuery(rqCxt, query, new HashMap());

        runningQuery.run();

    }
}

