package no.schibstedsok.searchportal.result;

import no.schibstedsok.searchportal.mode.command.SearchCommand;
import no.schibstedsok.searchportal.view.spell.QuerySuggestion;
import no.schibstedsok.searchportal.view.spell.SpellingSuggestion;

import java.util.List;
import java.util.Map;


/*
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 *
 */
public interface SearchResult {
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

    void addSpellingSuggestion(SpellingSuggestion suggestion);

    Map<String,List<SpellingSuggestion>> getSpellingSuggestions();

    List<QuerySuggestion> getQuerySuggestions();

    void addQuerySuggestion(QuerySuggestion query);

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
