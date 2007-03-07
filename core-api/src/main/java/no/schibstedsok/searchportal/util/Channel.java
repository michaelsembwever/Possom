// Copyright (2007) Schibsted Søk AS
/*
 * Channel.java
 *
 * Created on 09 November 2006, 10:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.util;

/**
 * Immutable channel class.
 * @author andersjj
 */

public class Channel implements Comparable {
    /** Channel id */
    private final String id;
    /** Channel name */
    private final String name;
    /** Channel priority */
    private final Integer priority;
    /** Channel category */
    private final Category category;
    
    /** Channel category enumeration */
    public enum Category {
        NORWEGIAN(),
        NORDIC(),
        INTERNATIONAL(),
        MOVIE(),
        NEWS(),
        SPORT(),
        MUSIC(),
        NATURE(),
        CHILDREN()
    }
    
    private Channel(final String id, final String name, final Integer priority, final Category category) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.category = category;
    }

    /** Create a new instance of a channel object */
    /* TODO: cache objects */
    public static final Channel newInstance(final String id, final String name, final int priority, final Category category) {
        return new Channel(id, name, priority, category);
    }

    public final String getId() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    public final Integer getPriority() {
        return this.priority;
    }

    public final Category getCategory() {
        return this.category;
    }
    
    public final int compareTo(Object o) {
        final Channel other = (Channel) o;
        
        if (other.getPriority() != this.getPriority()) {
            return this.getPriority() - other.getPriority();
        }
    
        if (other.getCategory() != this.getCategory()) {
            return this.getCategory().compareTo(other.getCategory());
        }
        
        return this.getName().compareTo(other.getName());
    }
}
