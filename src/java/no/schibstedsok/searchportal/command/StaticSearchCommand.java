/*
 * StaticSearchCommand.java
 *
 * Created on May 18, 2006, 10:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.command;

import java.util.Map;
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
    
    public StaticSearchCommand(final SearchCommand.Context cxt, final Map parameters) {
        super(cxt, parameters);
    }

    public SearchResult execute() {
        SearchResult result = new BasicSearchResult(this);
        result.addResult(dummyItem);
        result.setHitCount(1);
        return result;
    }
}
