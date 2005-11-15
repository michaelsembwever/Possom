/*
 * Copyright (2005) Schibsted S¿k AS
 */
package no.schibstedsok.front.searchportal.configuration;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.YellowSearchCommand;
import no.schibstedsok.front.searchportal.query.RunningQuery;

/**
 *
 * An implementation of Search Configuration for yellow searches.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class YellowSearchConfiguration extends FastConfiguration implements
        SearchConfiguration {

    private static Log log = LogFactory.getLog(YellowSearchConfiguration.class);
    
   
    
    /**
     *
     * Creates a new yellow search command.
     *
     * @param   query   the query to act on.
     * @param   parameters  the parameters.
     *
     * @return a new search command for.
     *
     */
    public SearchCommand createCommand(final RunningQuery query,
            final Map parameters) {
        return new YellowSearchCommand(query, this, parameters);
    }
}
