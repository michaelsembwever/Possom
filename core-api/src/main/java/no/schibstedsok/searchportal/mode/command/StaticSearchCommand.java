/*
 * StaticSearchCommand.java
 *
 * Created on May 18, 2006, 10:47 AM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;
import no.schibstedsok.searchportal.result.SearchResultItem;

/**
 * A search command that can be used to generate static HTML search results. No
 * search is done.
 *
 * @author maek
 */
public class StaticSearchCommand extends AbstractSearchCommand {

    private static final SearchResultItem dummyItem
            = new BasicSearchResultItem();

    public StaticSearchCommand(
            final SearchCommand.Context cxt,
            final DataModel datamodel) {

        super(cxt, datamodel);
    }

    public SearchResult execute() {
        SearchResult result = new BasicSearchResult(this);
        result.addResult(dummyItem);
        result.setHitCount(1);
        return result;
    }
}
