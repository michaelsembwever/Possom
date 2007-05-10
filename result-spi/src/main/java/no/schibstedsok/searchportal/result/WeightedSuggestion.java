/*
 * WeightedSuggestion.java
 * 
 * Created on 10/05/2007, 13:31:14
 * 
 */

package no.schibstedsok.searchportal.result;

/**
 *
 * @author mick
 * @version $Id$
 */
public interface WeightedSuggestion extends Suggestion, Comparable<WeightedSuggestion>{

    /**
     * 
     * @return 
     */
    int getWeight();
}
