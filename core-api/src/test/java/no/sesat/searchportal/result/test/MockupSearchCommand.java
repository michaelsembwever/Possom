/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.searchportal.result.test;

import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.searchportal.mode.command.SearchCommand;
import no.sesat.searchportal.mode.config.SearchConfiguration;
import no.sesat.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.sesat.searchportal.run.RunningQuery;
import no.sesat.searchportal.run.RunningQueryImpl;
import java.util.Properties;
import no.sesat.searchportal.datamodel.DataModel;
import no.sesat.searchportal.datamodel.DataModelTestCase;
import no.sesat.searchportal.mode.SearchMode;
import no.sesat.searchportal.result.ResultItem;
import no.sesat.searchportal.result.ResultList;
import no.sesat.searchportal.site.config.*;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.view.config.SearchTab;
import no.sesat.searchportal.view.SearchTabFactory;

/** Create a Mockup SearchCommand.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class MockupSearchCommand extends DataModelTestCase implements SearchCommand {

    private final RunningQuery.Context rqCxt = new RunningQuery.Context() {

        private final SearchMode mode = new SearchMode();
        private final DataModel datamodel = getDataModel();

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
        public BytecodeLoader newBytecodeLoader(SiteContext context, String className, String jar) {
            return UrlResourceLoader.newBytecodeLoader(context, className, jar);
        }

        public DataModel getDataModel(){
                return datamodel;
            }
    };

    private RunningQuery query;

    public MockupSearchCommand() throws SiteKeyedFactoryInstantiationException {
        query = new RunningQueryImpl(rqCxt, "");
    }

    public MockupSearchCommand(final String queryString) throws SiteKeyedFactoryInstantiationException {
        query = new RunningQueryImpl(rqCxt, queryString);
    }

    public SearchConfiguration getSearchConfiguration() {
        return null;
    }

    public RunningQuery getRunningQuery() {
        return query;
    }

    public ResultList<ResultItem> execute() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResultList<ResultItem> call() throws Exception {
        return null;
    }

    public boolean handleCancellation() {
        return false;
    }
}
