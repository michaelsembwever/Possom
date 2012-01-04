/* Copyright (2007-2012) Schibsted ASA
 *   This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ResultList.java
 *
 * Created on 10/05/2007, 12:59:32
 *
 */

package no.sesat.search.result;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;

/** A list of ResultItems.
 * The list itself is a "result" that contain properties (suggestions, relevant queries, etc).
 *
 * @param <T> the result item type the list contains.
 * @version $Id$
 */
public interface ResultList<T extends ResultItem> extends ResultItem{

    /** Get the hitcount. May not match getResults().size().
     *
     * @return
     */
    int getHitCount();

    /** Set the hitcount.
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
     * @param comparator
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
     * {@inheritDoc}
     * @param name {@inheritDoc}
     * @param value {@inheritDoc}
     */
    @Override
    ResultList<T> addField(String name, String value);

    /** Opposed to the superinterface, ResultLists can mutate and this method will return itself.
     * {@inheritDoc}
     * @param name {@inheritDoc}
     * @param value {@inheritDoc}
     */
    @Override
    ResultList<T> addObjectField(String name, Serializable value);
}
