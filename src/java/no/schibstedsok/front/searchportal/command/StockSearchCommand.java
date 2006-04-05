/*
 * StockSearchCommand.java
 *
 */

package no.schibstedsok.front.searchportal.command;

import java.util.Map;

import no.schibstedsok.front.searchportal.result.BasicSearchResult;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.SearchResult;

/**
 *
 * @author magnuse
 */
public final class StockSearchCommand extends AbstractSearchCommand {

    public StockSearchCommand(final Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {
        SearchResult result = new BasicSearchResult(this);
        result.addResult(new BasicSearchResultItem());
        result.setHitCount(1);
        return result;
    }
}
