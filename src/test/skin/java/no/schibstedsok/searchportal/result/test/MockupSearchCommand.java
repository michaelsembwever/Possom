// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.test;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.util.config.DocumentLoader;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.run.RunningQueryImpl;
import no.schibstedsok.searchportal.result.SearchResult;

import java.util.HashMap;
import java.util.Properties;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.util.config.FileResourceLoader;
import no.schibstedsok.searchportal.util.config.PropertiesLoader;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;

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

    public boolean handleCancellation() {
        return false;
    }
}
