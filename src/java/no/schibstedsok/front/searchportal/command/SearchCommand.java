/*
* Copyright (2005) Schibsted Søk AS
*
*/
package no.schibstedsok.front.searchportal.command;

import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.query.RunningQuery;
import no.schibstedsok.front.searchportal.site.SiteContext;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchCommand extends Callable {
    
    /** Being a factory for all the commands - it propagates all the contextual needs of the underlying commands it 
     * creates.
     */
    public interface Context extends SiteContext, ResourceContext{
        SearchConfiguration getSearchConfiguration();
        RunningQuery getQuery();
    }

    /**
     * Returns the configuration associated with this search command.
     *
     * @return The search configuration.
     */
    SearchConfiguration getSearchConfiguration();

    /**
     * Returns the query on which this command is acting.
     *
     * @return The query.
     */
    RunningQuery getQuery();
}
