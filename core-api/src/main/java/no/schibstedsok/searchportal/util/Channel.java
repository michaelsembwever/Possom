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

    private Channel(final String id, final String name, final Integer priority) {
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    /** Create a new instance of a channel object */
    /* TODO: cache objects */
    public static final Channel newInstance(String id, String name, int priority) {
        return new Channel(id, name, priority);
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

    public final int compareTo(Object o) {
        final Channel other = (Channel) o;
        
        if (other.getPriority() != this.getPriority()) {
            return this.getPriority() - other.getPriority();
        }
        
        return this.getName().compareTo(other.getName());
    }
}
