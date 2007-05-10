// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.Map;
import no.schibstedsok.searchportal.mode.command.SearchCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private int hitCount = -1;
    private final List<SearchResultItem> results = new ArrayList<SearchResultItem>();
    private final Map<String,List<WeightedSuggestion>> spellingSuggestions = new HashMap<String,List<WeightedSuggestion>>();
    private final List<Suggestion> querySuggestions = new ArrayList<Suggestion>();

    private final Map<String, String> fields = new HashMap();
    private List relevantQueries = new ArrayList();    

    /** TODO comment me. **/
    public BasicSearchResult(final SearchCommand command) {
        this.searchCommand = command;
    }

    /** {@inheritDoc} **/
    public SearchCommand getSearchCommand() {
        return searchCommand;
    }

    /** {@inheritDoc} **/
    public void setHitCount(final int docCount) {
        this.hitCount = docCount;
    }

    /** {@inheritDoc} **/
    public int getHitCount() {
        return hitCount;
    }

    /** {@inheritDoc} **/
    public void addResult(final SearchResultItem item) {
        results.add(item);
    }

    /** {@inheritDoc} **/
    public void addSpellingSuggestion(final WeightedSuggestion suggestion) {
        
        if (spellingSuggestions.containsKey(suggestion.getOriginal())) {
            final List<WeightedSuggestion> exising = spellingSuggestions.get(suggestion.getOriginal());
            exising.add(suggestion);
        } else {
            final List<WeightedSuggestion> existingSuggestions = new ArrayList<WeightedSuggestion>();
            existingSuggestions.add(suggestion);
            spellingSuggestions.put(suggestion.getOriginal(), existingSuggestions);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Spelling suggestions " + suggestion + " " + "added");
        }
    }

    /** {@inheritDoc} **/
    public List<WeightedSuggestion> getSpellingSuggestions() {
        
        final List<WeightedSuggestion> result = new ArrayList<WeightedSuggestion>();
        for(List<WeightedSuggestion> v : spellingSuggestions.values()){
            result.addAll(v);
        }
        return result;
    }
    
    /** {@inheritDoc} **/
    public Map<String,List<WeightedSuggestion>> getSpellingSuggestionsMap() {
        return spellingSuggestions;
    }

    /** {@inheritDoc} **/
    public Collection<Suggestion> getQuerySuggestions() {
       return querySuggestions;
    }

    /** {@inheritDoc} **/
    public void addQuerySuggestion(final Suggestion query) {
        querySuggestions.add(query);
    }

    /** {@inheritDoc} **/
    public List<SearchResultItem> getResults() {
        return results;
    }

    /** {@inheritDoc} **/
    public void addField(final String name, final String value) {
        fields.put(name, value);
    }

    /** {@inheritDoc} **/
    public String getField(final String name){
        return fields.get(name);
    }
    

    /**
     * 
     * @param query 
     */
    public void addRelevantQuery(final WeightedSuggestion query) {
        relevantQueries.add(query);
    }

    /**
     * Get the relevantQueries.
     *
     * @return the relevantQueries.
     */
    public List<WeightedSuggestion> getRelevantQueries() {
        
        Collections.sort(relevantQueries);
        return relevantQueries;
    }

    public Collection<String> getFieldNames() {
        
        return fields.keySet();
    }
}


