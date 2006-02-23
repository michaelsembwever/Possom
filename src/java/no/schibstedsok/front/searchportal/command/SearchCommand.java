/*
* Copyright (2005-2006) Schibsted Søk AS
*
*/
package no.schibstedsok.front.searchportal.command;

import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.SearchConfigurationContext;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.query.QueryContext;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;
import no.schibstedsok.front.searchportal.query.run.RunningQueryContext;
import no.schibstedsok.front.searchportal.site.SiteContext;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchCommand extends Callable {

    /** Being a factory for all the commands - it propagates all the contextual needs of the underlying commands it
     * creates.
     */
    public interface Context extends BaseContext, QueryContext, ResourceContext, RunningQueryContext,
            SearchConfigurationContext, SiteContext {
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
}
