package no.schibstedsok.front.searchportal.configuration;

import junit.framework.TestCase;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.executor.ParallelSearchCommandExecutor;
import no.schibstedsok.front.searchportal.result.TextOutputResultHandler;

import java.util.HashMap;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchModeTest extends TestCase {

    public void testWebCrawl() {

        SearchMode mode = new SearchMode();

        mode.setExecutor(new ParallelSearchCommandExecutor());

        FastConfiguration webCrawl = new FastConfiguration();

        webCrawl.setQueryServerURL("http://localhost:15100");
        webCrawl.addColletion("webcrawlno1");
        webCrawl.addColletion("webcrawlno1deep1");
        webCrawl.addColletion("webcrawlno2");
        webCrawl.addResultHandler(new TextOutputResultHandler());
        webCrawl.addResultField("url");
        webCrawl.addResultField("title");
        webCrawl.addResultField("body");
        webCrawl.setResultsToReturn(10);

        mode.addSearchConfiguration(webCrawl);

        RunningQuery query = new RunningQuery(mode, "aetat.no", new HashMap());

        try {
            query.run();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}

