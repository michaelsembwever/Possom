package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.spell.SpellingSuggestion;
import no.schibstedsok.front.searchportal.spell.QuerySuggestion;

import java.util.List;
import java.util.HashMap;

/*
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 *
 */

public interface SearchResult {
    /**
     * Returns the {@link SearchCommand} that produced this result.
     *
     * @return the search command.
     */
    SearchCommand getSearchCommand();

    int getHitCount();

    void setHitCount(int hitCount);

    List getResults();

    void addResult(SearchResultItem item);

    void addSpellingSuggestion(SpellingSuggestion suggestion);

    HashMap getSpellingSuggestions();

    List getQuerySuggestions();
    void addQuerySuggestion(QuerySuggestion query);
}
