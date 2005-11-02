package no.schibstedsok.front.searchportal.result.test;

import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.configuration.SearchConfiguration;
import no.schibstedsok.front.searchportal.query.RunningQuery;

import java.util.HashMap;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class MockupSearchCommand implements SearchCommand {

    RunningQuery query = new RunningQuery(null, "", new HashMap());

    public MockupSearchCommand() {
    }

    public MockupSearchCommand(String queryString) {
        query = new RunningQuery(null, queryString, new HashMap());
    }

    public SearchConfiguration getSearchConfiguration() {
        return null;
    }

    public RunningQuery getQuery() {
        return query;
    }

    public Object call() throws Exception {
        return null;
    }
}
