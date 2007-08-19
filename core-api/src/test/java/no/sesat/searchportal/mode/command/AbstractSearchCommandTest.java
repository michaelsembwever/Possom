/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
/*
 * AbstractSearchCommandTest.java
 *
 * Created on 8 May 2006, 06:26
 *
 */

package no.sesat.searchportal.mode.command;

import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.commons.ioc.ContextWrapper;
import no.sesat.searchportal.datamodel.DataModel;
import no.sesat.searchportal.datamodel.DataModelTestCase;
import no.sesat.searchportal.mode.config.SearchConfiguration;
import no.sesat.searchportal.mode.SearchMode;
import no.sesat.searchportal.mode.SearchModeFactory;
import no.sesat.searchportal.query.token.TokenEvaluationEngine;
import no.sesat.searchportal.site.config.DocumentLoader;
import no.sesat.searchportal.site.config.FileResourceLoader;
import no.sesat.searchportal.site.config.PropertiesLoader;
import no.sesat.searchportal.site.config.BytecodeLoader;
import no.sesat.searchportal.run.RunningQuery;
import no.sesat.searchportal.run.RunningQueryImpl;
import no.sesat.searchportal.site.Site;
import no.sesat.searchportal.site.SiteContext;
import no.sesat.searchportal.site.SiteKeyedFactoryInstantiationException;
import no.sesat.searchportal.view.config.SearchTab;
import no.sesat.searchportal.view.SearchTabFactory;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
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

        final DataModel datamodel = getDataModel();

        final SiteContext siteCxt = new SiteContext(){
            public Site getSite() {
                return datamodel.getSite().getSite();
            }
        };

        return new RunningQuery.Context() {
            public DataModel getDataModel(){
                return datamodel;
            }
            public SearchMode getSearchMode() {
                return SearchModeFactory.valueOf(
                        ContextWrapper.wrap(SearchModeFactory.Context.class, this, siteCxt))
                        .getMode(getSearchTab().getMode());
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.valueOf(
                    ContextWrapper.wrap(SearchTabFactory.Context.class, this, siteCxt))
                    .getTabByKey(key);
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

        final RunningTestQuery rq = new RunningTestQuery(rqCxt, query);

        final TokenEvaluationEngine engine = rq.getTokenEvaluationEngine();

        return createCommandContext(rq, rqCxt, conf);

    }

    protected final SearchCommand.Context createCommandContext(
            final RunningTestQuery rq,
            final RunningQuery.Context rqCxt,
            final String conf) throws SiteKeyedFactoryInstantiationException{

        final TokenEvaluationEngine engine = rq.getTokenEvaluationEngine();

        return ContextWrapper.wrap(
                SearchCommand.Context.class,
                new BaseContext() {
                    public SearchConfiguration getSearchConfiguration() {
                        return rqCxt.getSearchMode().getSearchConfiguration(conf);
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
