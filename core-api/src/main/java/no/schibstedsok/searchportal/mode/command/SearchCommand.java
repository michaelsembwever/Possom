/*
* Copyright (2005-2007) Schibsted Søk AS
*
*/
package no.schibstedsok.searchportal.mode.command;

import java.util.concurrent.Callable;
import no.schibstedsok.commons.ioc.BaseContext;
import no.schibstedsok.searchportal.datamodel.DataModelContext;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.mode.config.SearchConfigurationContext;
import no.schibstedsok.searchportal.site.config.ResourceContext;
import no.schibstedsok.searchportal.query.token.TokenEvaluationEngineContext;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.run.RunningQuery;
import no.schibstedsok.searchportal.run.RunningQueryContext;

/** Behavour requirements for any implementation Search Command.
 * 
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public interface SearchCommand extends Callable<ResultList<? extends ResultItem>> {

    /** Being a factory for all the commands - it propagates all the contextual needs of the underlying commands it
     * creates.
     */
    public interface Context extends BaseContext, ResourceContext, RunningQueryContext, DataModelContext,
            SearchConfigurationContext, TokenEvaluationEngineContext {
    }

    /**
     * Returns the configuration associated with this search command.
     *
     * @return The search configuration.
     */
    SearchConfiguration getSearchConfiguration();

    /**
     * Returns the query on which this command is acting.
     * @deprecated use the context instead. The dependency should be published like this.
     * @return The query.
     */
    RunningQuery getRunningQuery();

    /** Allows the SearchCommand to clean itself after a long night out when s/he didn't get home in time.
     * @return if cleaning was actually performed
     **/
    boolean handleCancellation();
}
