/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * WeightedSuggestion.java
 * 
 * Created on 10/05/2007, 13:31:14
 * 
 */

package no.sesat.search.result;

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
