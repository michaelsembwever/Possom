/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
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
