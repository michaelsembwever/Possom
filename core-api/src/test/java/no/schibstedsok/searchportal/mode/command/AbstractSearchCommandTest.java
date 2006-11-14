// Copyright (2006) Schibsted Søk AS
/*
 * AbstractSearchCommandTest.java
 *
 * Created on 8 May 2006, 06:26
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import no.schibstedsok.searchportal.TestCase;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.common.ioc.ContextWrapper;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.mode.config.SearchMode;
import no.schibstedsok.searchportal.mode.SearchModeFactory;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngine;
import no.schibstedsok.searchportal.site.config.DocumentLoader;
import no.schibstedsok.searchportal.site.config.FileResourceLoader;
import no.schibstedsok.searchportal.site.config.PropertiesLoader;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.run.RunningQueryImpl;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.view.config.SearchTab;
import no.schibstedsok.searchportal.view.config.SearchTabFactory;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractSearchCommandTest extends TestCase {


    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /** Creates a new instance of AbstractSearchCommandTest */
    public AbstractSearchCommandTest(final String testName) {
        super(testName);
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    protected final RunningQuery.Context createRunningQueryContext(final String key){

        return new RunningQuery.Context() {
            private final SearchMode mode = new SearchMode();

            public SearchMode getSearchMode() {
                return SearchModeFactory.valueOf(
                        ContextWrapper.wrap(SearchModeFactory.Context.class, this))
                        .getMode(getSearchTab().getMode());
            }
            public SearchTab getSearchTab(){
                return SearchTabFactory.valueOf(
                    ContextWrapper.wrap(SearchTabFactory.Context.class, this))
                    .getTabByKey(key);
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
    }

    protected final SearchCommand.Context createCommandContext(
            final String query,
            final RunningQuery.Context rqCxt,
            final String conf) {

        final RunningTestQuery rq = new RunningTestQuery(rqCxt, query, new HashMap());
        final Query q = rq.getQuery();
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
                    public Query getQuery(){
                        return q;
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
            final String conf) {

        final RunningQuery.Context rqCxt = createRunningQueryContext(key);

        return createCommandContext(query, rqCxt, conf);
    }

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
    
    public static final class RunningTestQuery extends RunningQueryImpl{
        
        public RunningTestQuery(final Context cxt, final String query, final Map parameters) {
            super(cxt, query, parameters);
        }
        
        public TokenEvaluationEngine getTokenEvaluationEngine(){
            return engine;
        }
    }
}
