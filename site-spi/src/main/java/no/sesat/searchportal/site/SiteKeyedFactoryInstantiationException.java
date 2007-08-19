/*
 * SiteKeyedFactoryInstantiationException.java
 *
 * Created on 23 January 2007, 19:07
 *
 */

package no.sesat.searchportal.site;

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
