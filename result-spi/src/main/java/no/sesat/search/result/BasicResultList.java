/* Copyright (2006-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.result;

import java.io.Serializable;
import java.util.Map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * A simple implementation of a search result.
 * Is not multi-thread safe. 
 * All fields (of all types) handled by superclass BasicSearchResultItem.
 *
 * @param T the type of ResultItem the ResultList contains.
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public class BasicResultList<T extends ResultItem> extends BasicResultItem implements ResultList<T> {

    private static final Logger LOG = Logger.getLogger(BasicResultList.class);

    private int hitCount = -1;
    
    private final List<T> results = new ArrayList<T>();
    
    private final Map<String,List<WeightedSuggestion>> spellingSuggestions 
            = new HashMap<String,List<WeightedSuggestion>>();
    
    private final List<Suggestion> querySuggestions = new ArrayList<Suggestion>();

    private final List<WeightedSuggestion> relevantQueries = new ArrayList<WeightedSuggestion>(); 
    
    /** Plain constructor.
     * 
     */
    public BasicResultList(){}
    
    protected BasicResultList(final String title, final String url, final int hitCount){
        super(title, url);
        this.hitCount = hitCount;
    }
    
    /** Copy constructor. 
     * Does not copy results, spellingSuggestions, querySuggestions, or relevantQueries.
     *
     * ** @param copy 
     */
    public BasicResultList(final ResultItem copy){
        super(copy);
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
    public void addResult(final T item) {
        results.add(item);
    }

    public void addResults(List<? extends T> items){
        results.addAll(items);
    }
    
    public void replaceResult(final T original, final T theNew){
        
        if(original != theNew){
            // if the instances vary then replace
            results.set(results.indexOf(original), theNew);
        }
    }
    
    public void removeResult(final T item){
        results.remove(item);
    }

    public void removeResults(){
        results.clear();
    }

    public void sortResults(final Comparator comparator){
        Collections.sort(results, comparator);
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
       return Collections.unmodifiableList(querySuggestions);
    }

    /** {@inheritDoc} **/
    public void addQuerySuggestion(final Suggestion query) {
        querySuggestions.add(query);
    }

    /** {@inheritDoc} **/          
    public List<T> getResults() {
        return Collections.unmodifiableList(results);
    }
    
    /** JavaBean compatability for JSPs. **/
    public int getResultsSize(){
        return results.size();
    }

    /** {@inheritDoc} **/
    @Override
    public BasicResultList<T> addField(final String field, final String value) {
        
        super.addField(field, value);
        return this;
    }

    @Override
    public BasicResultList<T> addObjectField(final String field, final Serializable value) {
        
        super.addObjectField(field, value);
        return this;
    }    

    @Override
    public BasicResultList<T> addToMultivaluedField(final String field, final String value) {
        
        super.addToMultivaluedField(field, value);
        return this;
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
        return Collections.unmodifiableList(relevantQueries);
    }

}


