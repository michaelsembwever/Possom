// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.configuration.FastNavigator;
import no.schibstedsok.front.searchportal.view.config.SearchTab;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class Modifier implements Comparable {
    private int count = 0;

    final private String name;
    final private FastNavigator navigator;

    public Modifier(final String name, final int count, final FastNavigator navigator) {
        this.name = name;
        this.count = count;
        this.navigator = navigator;
    }

    public Modifier(final String name, final FastNavigator navigator) {
        this.name = name;
        this.navigator = navigator;
    }

    
    public void addCount(final int count) {
        this.count += count;
    }

    public FastNavigator getNavigator() {
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

}
