// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.test;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.DocumentLoader;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.query.run.RunningQueryImpl;
import no.schibstedsok.front.searchportal.result.SearchResult;

import java.util.HashMap;
import java.util.Properties;
import no.schibstedsok.front.searchportal.configuration.SearchMode;
import no.schibstedsok.front.searchportal.configuration.loader.FileResourceLoader;
import no.schibstedsok.front.searchportal.configuration.loader.PropertiesLoader;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.view.config.SearchTab;
import no.schibstedsok.front.searchportal.view.config.SearchTabFactory;

/** Create a Mockup SearchCommand.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupSearchCommand implements SearchCommand {

    private final RunningQuery.Context rqCxt = new RunningQuery.Context() {

        private final SearchMode mode = new SearchMode();

        public SearchMode getSearchMode() {
            return mode;
        }
        public SearchTab getSearchTab(){
                return SearchTabFactory.getTabFactory(
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
    };

    private RunningQuery query;

    public MockupSearchCommand() {
        query = new RunningQueryImpl(rqCxt, "", new HashMap());
    }

    public MockupSearchCommand(final String queryString) {
        query = new RunningQueryImpl(rqCxt, queryString, new HashMap());
    }

    public SearchConfiguration getSearchConfiguration() {
        return null;
    }

    public RunningQuery getRunningQuery() {
        return query;
    }

    public SearchResult execute() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SearchResult call() throws Exception {
        return null;
    }

    public void handleCancellation() {
    }
}
