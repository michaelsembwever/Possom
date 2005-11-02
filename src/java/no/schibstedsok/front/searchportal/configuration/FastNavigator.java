package no.schibstedsok.front.searchportal.configuration;

import java.io.Serializable;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class FastNavigator implements Serializable {

    private String name;
    private String field;
    private FastNavigator childNavigator;
    private String displayName;

    public FastNavigator(String name, String field, String displayName) {
        this.name = name;
        this.field = field;
        this.displayName = displayName;
    }

    public FastNavigator() {
    }

    public FastNavigator getChildNavigator() {
        return childNavigator;
    }

    public void setChildNavigator(FastNavigator childNavigator) {
        this.childNavigator = childNavigator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
