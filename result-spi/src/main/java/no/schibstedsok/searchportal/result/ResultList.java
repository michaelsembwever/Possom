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
     * @return 
     */
    List<T> getResults();
    
    /**
     * 
     * @return 
     */
    List<WeightedSuggestion> getSpellingSuggestions();

    /**
     * 
     * @return 
     */
    Collection<Suggestion> getQuerySuggestions();

    /**
     * 
     * @return 
     */
    List<WeightedSuggestion> getRelevantQueries();
}
