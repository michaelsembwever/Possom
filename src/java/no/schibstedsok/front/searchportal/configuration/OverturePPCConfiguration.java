package no.schibstedsok.front.searchportal.configuration;

import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.command.OverturePPCCommand;
import no.schibstedsok.front.searchportal.query.run.RunningQuery;

import java.util.Map;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class OverturePPCConfiguration extends AbstractSearchConfiguration {

    private int resultsOnTop;

//    public SearchCommand createCommand(RunningQuery query, Map parameters) {
//        return new OverturePPCCommand(SearchCommand.Context ,parameters);
//    }

    public int getResultsOnTop() {
        return resultsOnTop;
    }

    public void setResultsOnTop(int resultsOnTop) {
        this.resultsOnTop = resultsOnTop;
    }
}
