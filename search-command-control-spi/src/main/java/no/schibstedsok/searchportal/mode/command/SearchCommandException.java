/*
 * Copyright (2005-2007) Schibsted Søk AS
 * 
 */
package no.schibstedsok.searchportal.mode.command;

/**
 * @author <a href="mailto:mick@semb.wever.org">Mck</a>
 * @version <tt>$Id$</tt>
 */
public class SearchCommandException extends RuntimeException {

    /**
     * Create a new InfrastructureException.
     * 
     * @param e
     */
    public SearchCommandException(Exception e) {
        super(e);
    }

    /**
     * Create a new InfrastructureException.
     * 
     * @param s
     * @param e
     */
    public SearchCommandException(String s, Exception e) {
        super(s, e);
    }
}
