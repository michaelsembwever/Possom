package no.sesat.searchportal.result;

import no.sesat.searchportal.result.Modifier;

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
