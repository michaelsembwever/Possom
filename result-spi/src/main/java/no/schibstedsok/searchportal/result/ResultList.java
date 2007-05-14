/*
 * ResultList.java
 * 
 * Created on 10/05/2007, 12:59:32
 * 
 */

package no.schibstedsok.searchportal.result;

import java.util.Collection;
import java.util.List;

/**
 *
 * @param T 
 * @author mick
 * @version $Id$
 */
public interface ResultList<T extends ResultItem> extends ResultItem{

    /**
     * 
     * @return 
     */
    int getHitCount();
    
    /**
     * 
     * @param hitCount 
     */
    void setHitCount(int hitCount);

    /** Returns a defensive copy of the results.
     * To update a ResultItem in the list use replaceResult(original, theNew).
     * 
     * @return 
     */
    List<T> getResults();
    
    /**
     * 
     * @param item 
     */
    void addResult(T item);
    
    /**
     * 
     * @param original 
     * @param theNew 
     */
    void replaceResult(T original, T theNew);
    
    /**
     * 
     * @param item 
     */
    void removeResult(T item);
    
    /**
     * 
     * @param item 
     */
    //void removeResult(T item);
    
    /**
     * 
     * @return 
     */
    List<WeightedSuggestion> getSpellingSuggestions();
    
    /**
     * 
     * @param suggestion 
     */
    void addSpellingSuggestion(WeightedSuggestion suggestion);
    
    
    /**
     * 
     * @param suggestion 
     */
    //void removeSpellingSuggestion(WeightedSuggestion suggestion);

    /**
     * 
     * @return 
     */
    Collection<Suggestion> getQuerySuggestions();
    
    /**
     * 
     * @param query 
     */
    void addQuerySuggestion(Suggestion query);
    
    /**
     * 
     * @param query 
     */
    //void removeQuerySuggestion(Suggestion query);

    /**
     * 
     * @return 
     */
    List<WeightedSuggestion> getRelevantQueries();
    
    /** Opposed to the superinterface, ResultLists can mutate and this method will return itself.
     * 
     * @param name 
     * @param value 
     */
    ResultList<T> addField(String name, String value);
    
}
