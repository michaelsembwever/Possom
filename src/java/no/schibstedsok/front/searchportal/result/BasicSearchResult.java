package no.schibstedsok.front.searchportal.result;

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

    private static Log log = LogFactory.getLog(BasicSearchResult.class);

    private SearchCommand searchCommand;
    protected int hitCount = -1;
    private List results = new ArrayList();
    private HashMap spellingSuggestions = new HashMap();
    private List querySuggestions = new ArrayList();
    private int analysisResult;

    public BasicSearchResult(SearchCommand command) {
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
            List exisingSuggestions = (List) spellingSuggestions.get(suggestion.getOriginal());
            exisingSuggestions.add(suggestion);
        } else {
            List existingSuggestions = new ArrayList();
            existingSuggestions.add(suggestion);
            spellingSuggestions.put(suggestion.getOriginal(), existingSuggestions);
        }

        if (log.isDebugEnabled()) {
            log.debug("Spelling suggestions " + suggestion + " " + "added");
        }
    }
    
    public HashMap getSpellingSuggestions() {
        return spellingSuggestions;
    }

    public List getQuerySuggestions() {
       return querySuggestions;
    }

    public void addQuerySuggestion(QuerySuggestion query) {
        querySuggestions.add(query);
    }

    public List getResults() {
        return results;
    }
}
