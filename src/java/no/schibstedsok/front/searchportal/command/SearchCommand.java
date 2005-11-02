/*
* Copyright (2005) Schibsted Sök AS
*
*/
package no.schibstedsok.front.searchportal.command;

import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.query.RunningQuery;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface SearchCommand extends Callable {

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
