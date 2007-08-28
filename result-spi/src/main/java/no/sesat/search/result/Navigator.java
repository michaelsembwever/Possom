/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.search.result;

import java.io.Serializable;

/** Configuration within a CommandConfig used for manipulating the command's navigation results.
 * 
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

    private final boolean boundaryMatch;


    public enum Sort {
        COUNT(),
        YEAR(),
        MONTH_YEAR(),
        DAY_MONTH_YEAR(),
        DAY_MONTH_YEAR_DESCENDING,
        YEAR_MONTH_DAY_DESCENDING,
        YEAR_MONTH,
        ALPHABETICAL,
        ALPHABETICAL_DESCENDING,
        CUSTOM,
        NONE
    }

    /**
     *
     */
    public Navigator(final String name, final String field, final String displayName, final Sort sort, final boolean boundaryMatch) {
        this.name = name;
        this.field = field;
        this.displayName = displayName;
        this.sort = sort;
        this.boundaryMatch = boundaryMatch;
    }

    /**
     *
     */
    public Navigator() {
        boundaryMatch = false;
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
    
    public boolean isBoundaryMatch() {
        return boundaryMatch;
    }
}
