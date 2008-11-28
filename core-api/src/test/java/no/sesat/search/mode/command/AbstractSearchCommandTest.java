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
 * AbstractSearchCommandTest.java
 *
 * Created on 8 May 2006, 06:26
 *
 */

package no.sesat.search.mode.command;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.sesat.commons.ioc.BaseContext;
import no.sesat.commons.ioc.ContextWrapper;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelTestCase;
import no.sesat.search.datamodel.access.ControlLevel;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.mode.SearchMode;
import no.sesat.search.mode.SearchModeFactory;
import no.sesat.search.query.token.TokenEvaluationEngine;
import no.sesat.search.site.config.DocumentLoader;
import no.sesat.search.site.config.FileResourceLoader;
import no.sesat.search.site.config.PropertiesLoader;
import no.sesat.search.site.config.BytecodeLoader;
import no.sesat.search.run.RunningQuery;
import no.sesat.search.run.RunningQueryImpl;
import no.sesat.search.site.Site;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.SiteKeyedFactoryInstantiationException;
import no.sesat.search.view.config.SearchTab;
import no.sesat.search.view.SearchTabFactory;

/**
 *
 *
 * @version $Id$
 */
public abstract class AbstractSearchCommandTest extends DataModelTestCase {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected final RunningQuery.Context createRunningQueryContext(final String key)
            throws SiteKeyedFactoryInstantiationException{

        final BaseContext cxt = createBaseRunningQueryContext();

        return ContextWrapper.wrap(
                RunningQuery.Context.class,
                cxt,
                new BaseContext(){
                    public SearchMode getSearchMode() {
                        return SearchModeFactory.instanceOf(
                                ContextWrapper.wrap(SearchModeFactory.Context.class, cxt))
                                .getMode(getSearchTab().getMode());
                    }
                    public SearchTab getSearchTab(){
                        return SearchTabFactory.instanceOf(
                            ContextWrapper.wrap(SearchTabFactory.Context.class, cxt))
                            .getTabByKey(key);
                    }
        });
    }

    protected final RunningQuery.Context createRunningQueryContext(final SearchTab key)
            throws SiteKeyedFactoryInstantiationException{

        final BaseContext cxt = createBaseRunningQueryContext();

        return ContextWrapper.wrap(
                RunningQuery.Context.class,
                cxt,
                new BaseContext(){
                    public SearchMode getSearchMode() {
                        return SearchModeFactory.instanceOf(
                                ContextWrapper.wrap(SearchModeFactory.Context.class, cxt))
                                .getMode(key.getMode());
                    }
                    public SearchTab getSearchTab(){
                        return key;
                    }
        });
    }

    private final BaseContext createBaseRunningQueryContext() throws SiteKeyedFactoryInstantiationException{

        final DataModel datamodel = getDataModel();

        return new BaseContext() {
            public Site getSite() {
                return datamodel.getSite().getSite();
            }
            public DataModel getDataModel(){
                return datamodel;
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
    }

    protected final SearchCommand.Context createCommandContext(
            final String query,
            final RunningQuery.Context rqCxt,
            final String conf) throws SiteKeyedFactoryInstantiationException{

        getDataModelFactory().assignControlLevel(rqCxt.getDataModel(), ControlLevel.RUNNING_QUERY_CONSTRUCTION);

        final RunningTestQuery rq = new RunningTestQuery(rqCxt, query);

        return createCommandContext(rq, rqCxt, conf);

    }

    protected final SearchCommand.Context createCommandContext(
            final RunningTestQuery rq,
            final RunningQuery.Context rqCxt,
            final String conf) throws SiteKeyedFactoryInstantiationException{

        getDataModelFactory().assignControlLevel(rqCxt.getDataModel(), ControlLevel.SEARCH_COMMAND_CONSTRUCTION);

        final TokenEvaluationEngine engine = rq.getTokenEvaluationEngine();

        return ContextWrapper.wrap(
                SearchCommand.Context.class,
                new BaseContext() {
                    public SearchConfiguration getSearchConfiguration() {

                        // unusual fallback through parent modes here.
                        // required as sometimes skins override a mode with the same name hiding
                        // a command configuration we are looking for
                        SearchMode mode = rqCxt.getSearchMode();
                        while(null == mode.getSearchConfiguration(conf) && null != mode.getParentSearchMode()){
                            mode = mode.getParentSearchMode();
                        }
                        return mode.getSearchConfiguration(conf);
                    }

                    public RunningQuery getRunningQuery() {
                        return rq;
                    }

                    public TokenEvaluationEngine getTokenEvaluationEngine(){
                        return engine;
                    }
                },
                rqCxt);
    }

    protected final SearchCommand.Context createCommandContext(
            final String query,
            final String key,
            final String conf) throws SiteKeyedFactoryInstantiationException {

        final RunningQuery.Context rqCxt = createRunningQueryContext(key);

        return createCommandContext(query, rqCxt, conf);
    }

    protected final SearchCommand.Context createCommandContext(
            final String query,
            final SearchTab key,
            final String conf) throws SiteKeyedFactoryInstantiationException {

        final RunningQuery.Context rqCxt = createRunningQueryContext(key);

        return createCommandContext(query, rqCxt, conf);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------

    public static final class RunningTestQuery extends RunningQueryImpl{

        public RunningTestQuery(
                final Context cxt,
                final String query) throws SiteKeyedFactoryInstantiationException {

            super(cxt, query);
        }

        public TokenEvaluationEngine getTokenEvaluationEngine(){
            return engine;
        }
    }
}
