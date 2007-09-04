/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
package no.sesat.search.result;

import no.sesat.search.result.Modifier;

import java.util.Comparator;

/**
 * @author Geir H. Pettersen(T-Rank)
 */
public enum ModifierStringComparator implements Comparator<Modifier> {
    ALPHABETICAL;
    public int compare(Modifier m1, Modifier m2) {
        return m1.getName().compareTo(m2.getName());
    }
}
