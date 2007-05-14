// Copyright (2007) Schibsted Søk AS
/*
 * StaticSearchCommand.java
 *
 * Created on May 18, 2006, 10:47 AM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;

/**
 * A search command that can be used to generate static HTML search results. No
 * search is done.
 *
 * @author maek
 */
public class StaticSearchCommand extends AbstractSearchCommand {

    private static final ResultItem DUMMYITEM = new BasicSearchResultItem();

    public StaticSearchCommand(final Context cxt) {

        super(cxt);
    }

    public ResultList<? extends ResultItem> execute() {
        
        final ResultList<ResultItem> result = new BasicSearchResult<ResultItem>();
        result.addResult(DUMMYITEM);
        result.setHitCount(1);
        return result;
    }
}
