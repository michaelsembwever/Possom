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

    public Modifier(String name, int count, FastNavigator navigator) {
        this.name = name;
        this.count = count;
        this.navigator = navigator;
    }

    public Modifier(String name, FastNavigator navigator) {
        this.name = name;
        this.navigator = navigator;
    }

    public void addCount(int count) {
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

    public int compareTo(Object o) {
        Modifier other = (Modifier) o;

        Integer otherCount = new Integer(other.getCount());
        Integer thisCount = new Integer(getCount());

        return otherCount.compareTo(thisCount);
    }

    public String toString() {
        return name + "(" + getCount() + ")";
    }

}
