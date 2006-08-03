// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.Map;
import no.schibstedsok.searchportal.command.SearchCommand;
import no.schibstedsok.searchportal.spell.SpellingSuggestion;
import no.schibstedsok.searchportal.spell.QuerySuggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class BasicSearchResult implements SearchResult {

    private static final Logger LOG = Logger.getLogger(BasicSearchResult.class);

    private final SearchCommand searchCommand;
    /** TODO comment me. **/
    protected int hitCount = -1;
    private final List<SearchResultItem> results = new ArrayList<SearchResultItem>();
    private final Map<String,List<SpellingSuggestion>> spellingSuggestions = new HashMap<String,List<SpellingSuggestion>>();
    private final List<QuerySuggestion> querySuggestions = new ArrayList<QuerySuggestion>();

    private final Map<String, String> fields = new HashMap();

    /** TODO comment me. **/
    public BasicSearchResult(final SearchCommand command) {
        this.searchCommand = command;
    }

    /** @inherit **/
    public SearchCommand getSearchCommand() {
        return searchCommand;
    }

    /** @inherit **/
    public void setHitCount(final int docCount) {
        this.hitCount = docCount;
    }

    /** @inherit **/
    public int getHitCount() {
        return hitCount;
    }

    /** @inherit **/
    public void addResult(final SearchResultItem item) {
        results.add(item);
    }

    /** @inherit **/
    public void addSpellingSuggestion(final SpellingSuggestion suggestion) {
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

    /** @inherit **/
    public Map<String,List<SpellingSuggestion>> getSpellingSuggestions() {
        return spellingSuggestions;
    }

    /** @inherit **/
    public List<QuerySuggestion> getQuerySuggestions() {
       return querySuggestions;
    }

    /** @inherit **/
    public void addQuerySuggestion(final QuerySuggestion query) {
        querySuggestions.add(query);
    }

    /** @inherit **/
    public List<SearchResultItem> getResults() {
        return results;
    }

    /** @inherit **/
    public void addField(final String name, final String value) {
        fields.put(name, value);
    }

    /** @inherit **/
    public String getField(final String name){
        return fields.get(name);
    }
}


