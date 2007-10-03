/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * ResultList.java
 * 
 * Created on 10/05/2007, 12:59:32
 * 
 */

package no.sesat.search.result;

import java.util.Comparator;
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
     * Implementations of this method are free to return a live copy and document such, 
     *  but the default defined behaviour is of an restricted API.
     * 
     * @return 
     */
    List<T> getResults();
    
    /** Adds the result to the end of the current list of results.
     * 
     * @param item 
     */
    void addResult(T item);

    /** Appends the results to the end of the current list of results.
     * 
     * @param item 
     */
    void addResults(List<? extends T> item);
    
    /** Replace the original with theNew.
     * 
     * @param original 
     * @param theNew 
     */
    void replaceResult(T original, T theNew);
    
    /** Remove the result from the current result list.
     * 
     * @param item 
     */
    void removeResult(T item);

    /** Remove all results from the current result list.
     * 
     **/
    void removeResults();

    /** Sorts the results according to the order induced by the specified comparator.
     */
    void sortResults(final Comparator comparator);
    
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
