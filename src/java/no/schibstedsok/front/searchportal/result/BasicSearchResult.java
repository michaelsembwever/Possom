package no.schibstedsok.front.searchportal.result;

import java.util.Map;
import no.schibstedsok.front.searchportal.command.SearchCommand;
import no.schibstedsok.front.searchportal.spell.SpellingSuggestion;
import no.schibstedsok.front.searchportal.spell.QuerySuggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class BasicSearchResult implements SearchResult {

    private static Log LOG = LogFactory.getLog(BasicSearchResult.class);

    private final SearchCommand searchCommand;
    protected int hitCount = -1;
    private final List<SearchResultItem> results = new ArrayList<SearchResultItem>();
    private final Map<String,List<SpellingSuggestion>> spellingSuggestions = new HashMap<String,List<SpellingSuggestion>>();
    private final List<QuerySuggestion> querySuggestions = new ArrayList<QuerySuggestion>();

    public BasicSearchResult(final SearchCommand command) {
        this.searchCommand = command;
    }

    public SearchCommand getSearchCommand() {
        return searchCommand;
    }

    public void setHitCount(int docCount) {
        this.hitCount = docCount;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void addResult(SearchResultItem item) {
        results.add(item);
    }

    public void addSpellingSuggestion(SpellingSuggestion suggestion) {
        if (spellingSuggestions.containsKey(suggestion.getOriginal())) {
            final List<SpellingSuggestion> exising = spellingSuggestions.get(suggestion.getOriginal());
            exising.add(suggestion);
        } else {
            final List<SpellingSuggestion> existingSuggestions = new ArrayList<SpellingSuggestion>();
            existingSuggestions.add(suggestion);
            spellingSuggestions.put(suggestion.getOriginal(), existingSuggestions);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Spelling suggestions " + suggestion + " " + "added");
        }
    }
    
    public Map<String,List<SpellingSuggestion>> getSpellingSuggestions() {
        return spellingSuggestions;
    }

    public List<QuerySuggestion> getQuerySuggestions() {
       return querySuggestions;
    }

    public void addQuerySuggestion(QuerySuggestion query) {
        querySuggestions.add(query);
    }

    public List<SearchResultItem> getResults() {
        return results;
    }
}
