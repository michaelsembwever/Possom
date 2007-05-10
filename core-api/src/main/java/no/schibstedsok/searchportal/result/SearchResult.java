// Copyright (2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.Collection;
import no.schibstedsok.searchportal.mode.command.SearchCommand;

import java.util.List;


/*
 * @deprecated ResultItem is the replacement. migration in progress.
 * @version <tt>$Id$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 *
 */
/*
 * @deprecated ResultItem is the replacement. migration in progress.
 * @version <tt>$Id$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 *
 */
public interface SearchResult extends ResultList{
    /**
     * Returns the {@link SearchCommand} that produced this result.
     * @deprecated should not be accessing search command from the view domain.
     * @return the search command.
     */
    SearchCommand getSearchCommand();

    int getHitCount();

    void setHitCount(int hitCount);

    List<SearchResultItem> getResults();

    void addResult(SearchResultItem item);

    void addSpellingSuggestion(WeightedSuggestion suggestion);

    List<WeightedSuggestion> getSpellingSuggestions();

    Collection<Suggestion> getQuerySuggestions();

    void addQuerySuggestion(Suggestion query);

    /**
     * Adds a result level (as opposed to item level) field to the search result.
     *
     */
    public void addField(String name, String value);
    
    /**
     * Gets the value of field.
     */
    public String getField(String name);

}
