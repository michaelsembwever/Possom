// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.util.Comparator;
import no.schibstedsok.searchportal.view.config.SearchTab;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class Modifier implements Comparable {
    private int count = 0;

    final private String name;
    final private Navigator navigator;

    private static final Comparator<Modifier> HINT_PRIO_COMPARATOR =
            new Comparator<Modifier>() {

        public int compare(final Modifier m1, final Modifier m2) {
            if (m1.getNavigationHint() == null || m2.getNavigationHint() == null) {
                return 0;
            }

            int p1 = m1.getNavigationHint().getPriority();
            int p2 = m2.getNavigationHint().getPriority();
             
             if (p1 == p2) {
                 return 0;
             }
             
             return p1 > p2 ? 1 : -1;
        }
    };

    public Modifier(final String name, final int count, final Navigator navigator) {
        this.name = name;
        this.count = count;
        this.navigator = navigator;
    }

    public Modifier(final String name, final Navigator navigator) {
        this.name = name;
        this.navigator = navigator;
    }

    
    public void addCount(final int count) {
        this.count += count;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public int compareTo(final Object o) {
        final Modifier other = (Modifier) o;

        final Integer otherCount = Integer.valueOf(other.getCount());
        final Integer thisCount = Integer.valueOf(getCount());

        return otherCount.compareTo(thisCount);
    }

    public String toString() {
        return name + "(" + getCount() + ")";
    }

    /**
     * Holds value of property navigationHint.
     */
    private SearchTab.NavigatorHint navigationHint;

    /**
     * Getter for property navigationHint.
     * @return Value of property navigationHint.
     */
    public SearchTab.NavigatorHint getNavigationHint() {
        return this.navigationHint;
    }

    /**
     * Setter for property navigationHint.
     * @param navigationHint New value of property navigationHint.
     */
    public void setNavigationHint(SearchTab.NavigatorHint navigationHint) {
        this.navigationHint = navigationHint;
    }

    public static Comparator<Modifier> getHintPriorityComparator() {
        return HINT_PRIO_COMPARATOR;
    }
}
