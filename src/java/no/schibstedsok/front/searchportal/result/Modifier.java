// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.configuration.FastNavigator;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class Modifier implements Comparable {
    private int count = 0;
    private String name;
    private FastNavigator navigator;

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

}
