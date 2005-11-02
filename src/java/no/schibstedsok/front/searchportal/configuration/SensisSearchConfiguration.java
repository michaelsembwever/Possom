package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.FastSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.query.RunningQuery;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SensisSearchConfiguration extends FastConfiguration {
    public SearchCommand createCommand(RunningQuery query, Map parameters) {
        return new FastSearchCommand(query, this, parameters);
    }
}
