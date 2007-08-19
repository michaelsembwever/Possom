/*
 * ResourceLoadException.java
 *
 * Created on October 23, 2006, 12:31 PM
 */

package no.sesat.search.site.config;

/**
 *
 * @author mick
 */
public final class ResourceLoadException extends RuntimeException{ // TODO this is not a RuntimeException!
    
    /** Creates a new instance of ResourceLoadException */
    private ResourceLoadException() {
    }
    
    /** Creates a new instance of ResourceLoadException */
    public ResourceLoadException(final String msg) {
        super(msg);
    }
    
    /** Creates a new instance of ResourceLoadException */
    public ResourceLoadException(final String msg, final Throwable th) {
        super(msg, th);
    }
    
}
