/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * SiteKeyedFactoryInstantiationException.java
 *
 * Created on 23 January 2007, 19:07
 *
 */

package no.sesat.search.site;

/**
 *
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public final class SiteKeyedFactoryInstantiationException extends Exception{
    
    public SiteKeyedFactoryInstantiationException(final String msg, final Exception cause){
        super(msg, cause);
    }
}
