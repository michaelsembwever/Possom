package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.PicSearchCommand;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.query.RunningQuery;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PicSearchConfiguration extends AbstractSearchConfiguration {
    public SearchCommand createCommand(RunningQuery query, Map parameters) {
        return new PicSearchCommand(query, this, parameters);
    }
}
