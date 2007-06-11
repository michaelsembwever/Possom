// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result;

import java.io.Serializable;

/**
 * @deprecated Geir's navigation model is the replacement.
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision: 3361 $</tt>
 */
public final class Navigator implements Serializable {

    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = -3135641813818854457L;
    private String name;
    private String field;
    private Navigator childNavigator;
    private String displayName;
    private Sort sort;

    public enum Sort {
        COUNT(),
        CHANNEL(),
        YEAR(),
        MONTH_YEAR(),
        DAY_MONTH_YEAR(),
        DAY_MONTH_YEAR_DESCENDING,
        YEAR_MONTH,
        NONE
    }

    /**
     *
     */
    public Navigator(final String name, final String field, final String displayName, final Sort sort) {
        this.name = name;
        this.field = field;
        this.displayName = displayName;
        this.sort = sort;
    }

    /**
     *
     */
    public Navigator() {
    }

    /**
     *
     */
    public Navigator getChildNavigator() {
        return childNavigator;
    }

    /**
     *
     */
    public void setChildNavigator(final Navigator childNavigator) {
        this.childNavigator = childNavigator;
    }

    /**
     *
     */
    public String getName() {
        return name;
    }

    /**
     *
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     *
     */
    public String toString() {
        return name;
    }

    /**
     *
     */
    public String getField() {
        return field;
    }

    /**
     *
     */
    public void setField(final String field) {
        this.field = field;
    }

    /**
     *
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get navigator sort by enum.
     *
     * @return sort enum.
     */
    public final Sort getSort() {
        return this.sort;
    }

    /**
     * Holds value of property id.
     */
    private String id;

    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Setter for property id.
     *
     * @param id New value of property id.
     */
    public void setId(final String id) {
        this.id = id;
    }
}
